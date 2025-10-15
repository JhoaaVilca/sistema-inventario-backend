package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.dto.KardexRequestDTO;
import tienda.inventario.modelo.Kardex;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.KardexRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KardexServicio {

    @Autowired
    private KardexRepositorio kardexRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    // Registrar un movimiento de entrada en el Kardex
    @Transactional
    public Kardex registrarEntrada(Producto producto, Integer cantidad, BigDecimal precioUnitario, String referenciaDocumento, String usuarioRegistro, String observaciones) {
        return registrarMovimiento(producto, "ENTRADA", cantidad, precioUnitario, referenciaDocumento, usuarioRegistro, observaciones);
    }

    // Registrar un movimiento de salida en el Kardex
    @Transactional
    public Kardex registrarSalida(Producto producto, Integer cantidad, BigDecimal precioUnitario, String referenciaDocumento, String usuarioRegistro, String observaciones) {
        return registrarMovimiento(producto, "SALIDA", cantidad, precioUnitario, referenciaDocumento, usuarioRegistro, observaciones);
    }

    // Registrar un movimiento de ajuste en el Kardex
    @Transactional
    public Kardex registrarAjuste(Producto producto, Integer cantidad, BigDecimal precioUnitario, String referenciaDocumento, String usuarioRegistro, String observaciones) {
        return registrarMovimiento(producto, "AJUSTE", cantidad, precioUnitario, referenciaDocumento, usuarioRegistro, observaciones);
    }

    @Transactional
    public Kardex registrarMovimiento(Producto producto, String tipoMovimiento, Integer cantidad, BigDecimal precioUnitario, String referenciaDocumento, String usuarioRegistro, String observaciones) {
        Kardex kardex = new Kardex();
        kardex.setProducto(producto);
        kardex.setTipoMovimiento(tipoMovimiento);
        kardex.setCantidad(cantidad);
        kardex.setPrecioUnitario(precioUnitario);
        kardex.setValorTotal(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
        kardex.setReferenciaDocumento(referenciaDocumento);
        kardex.setUsuarioRegistro(usuarioRegistro);
        kardex.setObservaciones(observaciones);

        // Obtener el último movimiento para calcular stock y costo promedio
        Optional<Kardex> ultimoMovimientoOpt = obtenerUltimoMovimiento(producto.getIdProducto());

        Integer stockAnterior = ultimoMovimientoOpt.map(Kardex::getStockActual).orElse(0);
        BigDecimal costoPromedioAnterior = ultimoMovimientoOpt.map(Kardex::getCostoPromedioActual).orElse(BigDecimal.ZERO);

        kardex.setStockAnterior(stockAnterior);
        kardex.setCostoPromedioAnterior(costoPromedioAnterior);

        Integer stockActual;
        BigDecimal costoPromedioActual;

        if ("ENTRADA".equals(tipoMovimiento) || "AJUSTE".equals(tipoMovimiento)) {
            stockActual = stockAnterior + cantidad;
            costoPromedioActual = Kardex.calcularCostoPromedioPonderado(
                    BigDecimal.valueOf(stockAnterior), costoPromedioAnterior,
                    precioUnitario, cantidad);
        } else if ("SALIDA".equals(tipoMovimiento)) {
            stockActual = stockAnterior - cantidad;
            // Para salidas, el costo promedio no cambia, se usa el anterior
            costoPromedioActual = costoPromedioAnterior;
        } else {
            throw new IllegalArgumentException("Tipo de movimiento de Kardex no válido: " + tipoMovimiento);
        }

        kardex.setStockActual(stockActual);
        kardex.setCostoPromedioActual(costoPromedioActual);

        // Actualizar el stock del producto
        producto.setStock(stockActual);
        productoRepositorio.save(producto);

        return kardexRepositorio.save(kardex);
    }

    // Obtener movimientos de Kardex por producto y rango de fechas
    public Page<Kardex> obtenerMovimientosPorProductoYFechas(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return kardexRepositorio.findByProductoIdProductoAndFechaMovimientoBetween(idProducto, fechaInicio, fechaFin, pageable);
    }

    // Obtener movimientos de Kardex por producto
    public Page<Kardex> obtenerMovimientosPorProducto(Long idProducto, Pageable pageable) {
        return kardexRepositorio.findByProductoIdProducto(idProducto, pageable);
    }

    // Obtener todos los movimientos de Kardex con paginación
    public Page<Kardex> obtenerTodosLosMovimientos(Pageable pageable) {
        return kardexRepositorio.findAll(pageable);
    }

    // Obtener movimientos de Kardex por rango de fechas (general)
    public Page<Kardex> obtenerMovimientosPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return kardexRepositorio.findByFechaMovimientoBetween(fechaInicio, fechaFin, pageable);
    }

    // Obtener último movimiento de un producto
    public Optional<Kardex> obtenerUltimoMovimiento(Long idProducto) {
        List<Kardex> movimientos = kardexRepositorio.findTop1ByProductoIdProductoOrderByFechaMovimientoDesc(idProducto);
        return movimientos.isEmpty() ? Optional.empty() : Optional.of(movimientos.get(0));
    }

    // Método para crear un ajuste manual
    @Transactional
    public Kardex crearAjusteManual(KardexRequestDTO request) {
        Producto producto = productoRepositorio.findById(request.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Obtener el último movimiento para calcular stock y costo promedio
        Optional<Kardex> ultimoMovimientoOpt = obtenerUltimoMovimiento(producto.getIdProducto());

        Integer stockAnterior = ultimoMovimientoOpt.map(Kardex::getStockActual).orElse(0);
        BigDecimal costoPromedioAnterior = ultimoMovimientoOpt.map(Kardex::getCostoPromedioActual).orElse(BigDecimal.ZERO);

        Integer cantidadAjuste = request.getCantidad();
        BigDecimal precioUnitarioAjuste = request.getPrecioUnitario() != null ? request.getPrecioUnitario() : BigDecimal.ZERO;

        Integer stockActual;
        BigDecimal costoPromedioActual;

        if ("ENTRADA".equals(request.getTipoMovimiento())) {
            stockActual = stockAnterior + cantidadAjuste;
            costoPromedioActual = Kardex.calcularCostoPromedioPonderado(
                    BigDecimal.valueOf(stockAnterior), costoPromedioAnterior,
                    precioUnitarioAjuste, cantidadAjuste);
        } else if ("SALIDA".equals(request.getTipoMovimiento())) {
            stockActual = stockAnterior - cantidadAjuste;
            if (stockActual < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo después del ajuste de salida.");
            }
            costoPromedioActual = costoPromedioAnterior; // El costo promedio no cambia en una salida
        } else {
            throw new IllegalArgumentException("Tipo de movimiento de ajuste no válido: " + request.getTipoMovimiento());
        }

        Kardex kardex = new Kardex();
        kardex.setProducto(producto);
        kardex.setFechaMovimiento(LocalDateTime.now());
        kardex.setTipoMovimiento(request.getTipoMovimiento());
        kardex.setCantidad(cantidadAjuste);
        kardex.setPrecioUnitario(precioUnitarioAjuste);
        kardex.setValorTotal(precioUnitarioAjuste.multiply(BigDecimal.valueOf(cantidadAjuste)));
        kardex.setStockAnterior(stockAnterior);
        kardex.setStockActual(stockActual);
        kardex.setCostoPromedioAnterior(costoPromedioAnterior);
        kardex.setCostoPromedioActual(costoPromedioActual);
        kardex.setObservaciones(request.getObservaciones());
        kardex.setReferenciaDocumento("AJUSTE MANUAL");
        kardex.setUsuarioRegistro(request.getUsuarioRegistro()); // Asignar el usuario que realiza el ajuste

        // Actualizar el stock del producto
        producto.setStock(stockActual);
        productoRepositorio.save(producto);

        return kardexRepositorio.save(kardex);
    }
}
