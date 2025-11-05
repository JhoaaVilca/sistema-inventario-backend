package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.dto.KardexRequestDTO;
import tienda.inventario.dto.KardexResumenDTO;
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

        return kardexRepositorio.save(kardex);
    }

    // Obtener movimientos de Kardex por producto y rango de fechas
    public Page<Kardex> obtenerMovimientosPorProductoYFechasYTipo(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoMovimiento, Pageable pageable) {
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            return kardexRepositorio.findByProductoIdProductoAndFechaMovimientoBetweenAndTipoMovimientoIgnoreCase(idProducto, fechaInicio, fechaFin, tipoMovimiento, pageable);
        }
        return kardexRepositorio.findByProductoIdProductoAndFechaMovimientoBetween(idProducto, fechaInicio, fechaFin, pageable);
    }

    // Obtener movimientos de Kardex por producto
    public Page<Kardex> obtenerMovimientosPorProductoYTipo(Long idProducto, String tipoMovimiento, Pageable pageable) {
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            return kardexRepositorio.findByProductoIdProductoAndTipoMovimientoIgnoreCase(idProducto, tipoMovimiento, pageable);
        }
        return kardexRepositorio.findByProductoIdProducto(idProducto, pageable);
    }

    // Obtener todos los movimientos de Kardex con paginación
    public Page<Kardex> obtenerTodosLosMovimientosPorTipo(String tipoMovimiento, Pageable pageable) {
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            return kardexRepositorio.findByTipoMovimientoIgnoreCase(tipoMovimiento, pageable);
        }
        return kardexRepositorio.findAll(pageable);
    }

    // Obtener movimientos de Kardex por rango de fechas (general)
    public Page<Kardex> obtenerMovimientosPorFechasYTipo(LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoMovimiento, Pageable pageable) {
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            return kardexRepositorio.findByFechaMovimientoBetweenAndTipoMovimientoIgnoreCase(fechaInicio, fechaFin, tipoMovimiento, pageable);
        }
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
        // Los ajustes manuales siempre se guardan como tipo "AJUSTE"
        kardex.setTipoMovimiento("AJUSTE");
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

    public Integer obtenerSaldoInicialProducto(Long idProducto, LocalDateTime fechaInicio) {
        if (idProducto == null) return 0;
        if (fechaInicio == null) {
            return obtenerUltimoMovimiento(idProducto).map(Kardex::getStockActual).orElse(0);
        }
        List<Kardex> prev = kardexRepositorio
                .findTop1ByProductoIdProductoAndFechaMovimientoBeforeOrderByFechaMovimientoDesc(idProducto, fechaInicio);
        return prev.isEmpty() ? 0 : prev.get(0).getStockActual();
    }

    public KardexResumenDTO calcularResumen(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoMovimiento) {
        KardexResumenDTO resumen = new KardexResumenDTO();
        resumen.setIdProducto(idProducto);

        int saldoInicial = obtenerSaldoInicialProducto(idProducto, fechaInicio);
        resumen.setSaldoInicial(saldoInicial);

        // Traer movimientos sin paginación segun filtros
        List<Kardex> movimientos;
        if (idProducto != null && fechaInicio != null && fechaFin != null) {
            movimientos = obtenerMovimientosPorProductoYFechasYTipo(idProducto, fechaInicio, fechaFin, tipoMovimiento, Pageable.unpaged()).getContent();
        } else if (idProducto != null) {
            movimientos = obtenerMovimientosPorProductoYTipo(idProducto, tipoMovimiento, Pageable.unpaged()).getContent();
        } else if (fechaInicio != null && fechaFin != null) {
            movimientos = obtenerMovimientosPorFechasYTipo(fechaInicio, fechaFin, tipoMovimiento, Pageable.unpaged()).getContent();
        } else {
            movimientos = obtenerTodosLosMovimientosPorTipo(tipoMovimiento, Pageable.unpaged()).getContent();
        }

        int entradasCant = 0;
        int salidasCant = 0;
        BigDecimal entradasVal = BigDecimal.ZERO;
        BigDecimal salidasVal = BigDecimal.ZERO;

        for (Kardex k : movimientos) {
            if ("SALIDA".equalsIgnoreCase(k.getTipoMovimiento())) {
                salidasCant += k.getCantidad();
                if (k.getValorTotal() != null) salidasVal = salidasVal.add(k.getValorTotal());
            } else if ("AJUSTE".equalsIgnoreCase(k.getTipoMovimiento())) {
                // Los ajustes pueden ser positivos (aumentan stock) o negativos (disminuyen stock)
                // Se determina comparando stockActual con stockAnterior
                if (k.getStockActual() != null && k.getStockAnterior() != null) {
                    if (k.getStockActual() > k.getStockAnterior()) {
                        // Ajuste positivo (entrada)
                        entradasCant += k.getCantidad();
                        if (k.getValorTotal() != null) entradasVal = entradasVal.add(k.getValorTotal());
                    } else if (k.getStockActual() < k.getStockAnterior()) {
                        // Ajuste negativo (salida)
                        salidasCant += k.getCantidad();
                        if (k.getValorTotal() != null) salidasVal = salidasVal.add(k.getValorTotal());
                    }
                    // Si son iguales, no se cuenta (ajuste neutro, aunque no debería pasar)
                }
            } else { // ENTRADA
                entradasCant += k.getCantidad();
                if (k.getValorTotal() != null) entradasVal = entradasVal.add(k.getValorTotal());
            }
            if (resumen.getNombreProducto() == null && k.getProducto() != null) {
                resumen.setNombreProducto(k.getProducto().getNombreProducto());
            }
        }

        int stockFinal = saldoInicial + entradasCant - salidasCant;
        resumen.setTotalEntradasCantidad(entradasCant);
        resumen.setTotalSalidasCantidad(salidasCant);
        resumen.setStockFinal(stockFinal);
        resumen.setTotalEntradasValor(entradasVal);
        resumen.setTotalSalidasValor(salidasVal);

        // costoPromedioFinal y costoTotalFinal
        BigDecimal costoPromFinal = BigDecimal.ZERO;
        if (idProducto != null) {
            costoPromFinal = Optional.ofNullable(kardexRepositorio.findCurrentAverageCostByProductId(idProducto))
                    .orElse(BigDecimal.ZERO);
        }
        resumen.setCostoPromedioFinal(costoPromFinal);
        resumen.setCostoTotalFinal(costoPromFinal.multiply(BigDecimal.valueOf(stockFinal)));
        resumen.setGananciaEstimada(null); // No calculable sin integrar ventas
        return resumen;
    }
}
