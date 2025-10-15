package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.modelo.DetalleSalida;
import tienda.inventario.modelo.Salida;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.DetalleSalidaRepositorio;
import tienda.inventario.repositorio.SalidaRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;
import tienda.inventario.repositorio.LoteRepositorio;
import tienda.inventario.repositorio.CreditoRepositorio;
import tienda.inventario.modelo.Credito;
import tienda.inventario.modelo.Lote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

@Service
public class SalidaServicio implements ISalidaServicio {

    @Autowired
    private SalidaRepositorio salidaRepositorio;

    @Autowired
    private DetalleSalidaRepositorio detalleSalidaRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private LoteRepositorio loteRepositorio;

    @Autowired
    private CreditoRepositorio creditoRepositorio;

    @Autowired
    private KardexServicio kardexServicio;

    @Override
    @Transactional
    public Salida guardarSalida(Salida salida) {
        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                detalle.setSalida(salida);
            }
        }

        // Validación de stock por producto contra lotes activos (FIFO) y cálculo de subtotales/total antes de guardar
        double totalCalculado = 0.0;
        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD == null) {
                    throw new RuntimeException("Producto no encontrado: id=" + detalle.getProducto().getIdProducto());
                }
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                if (cantidad <= 0) {
                    throw new RuntimeException("Cantidad inválida para el producto id=" + productoBD.getIdProducto());
                }
                // Validar stock por lotes activos (cantidadDisponible)
                var lotesActivos = loteRepositorio.findByProductoIdProducto(productoBD.getIdProducto());
                int disponiblePorLotes = lotesActivos.stream()
                        .map(l -> l.getCantidadDisponible() == null ? 0 : l.getCantidadDisponible())
                        .reduce(0, Integer::sum);
                if (disponiblePorLotes < cantidad) {
                    throw new RuntimeException("Stock insuficiente por lotes para el producto '" + productoBD.getNombreProducto() + "' (disponible=" + disponiblePorLotes + ", solicitado=" + cantidad + ")");
                }

                double precioUnitario = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0;
                double subtotal = cantidad * precioUnitario;
                detalle.setSubtotal(subtotal);
                totalCalculado += subtotal;
            }
        }
        salida.setTotalSalida(totalCalculado);

        // Estado por defecto al registrar: Completado
        if (salida.getEstado() == null || salida.getEstado().isBlank()) {
            salida.setEstado("Completado");
        }
        Salida nuevaSalida = salidaRepositorio.save(salida);

        // Descontar por lotes FIFO y actualizar stock global
        if (nuevaSalida.getDetalles() != null) {
            for (DetalleSalida detalle : nuevaSalida.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD == null) continue;

                int restante = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
                var lotesActivos = loteRepositorio.findByProductoIdProducto(productoBD.getIdProducto());
                for (Lote lote : lotesActivos) {
                    if (restante <= 0) break;
                    Integer disp = lote.getCantidadDisponible() == null ? 0 : lote.getCantidadDisponible();
                    if (disp <= 0) continue;
                    int aConsumir = Math.min(restante, disp);
                    lote.setCantidadDisponible(disp - aConsumir);
                    if (lote.getCantidadDisponible() != null && lote.getCantidadDisponible() <= 0) {
                        lote.setEstado("Agotado");
                    }
                    loteRepositorio.save(lote);
                    restante -= aConsumir;
                }

                // Actualizar stock global del producto para mantener consistencia con UI actual
                Integer stockActual = productoBD.getStock() != null ? productoBD.getStock() : 0;
                productoBD.setStock(Math.max(0, stockActual - (detalle.getCantidad() == null ? 0 : detalle.getCantidad())));
                productoRepositorio.save(productoBD);

                // Registrar movimiento en Kardex (Salida)
                try {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String username = auth != null ? auth.getName() : "system";
                    BigDecimal precioUnitario = BigDecimal.valueOf(detalle.getPrecioUnitario() == null ? 0.0 : detalle.getPrecioUnitario());
                    String referencia = "SALIDA " + nuevaSalida.getIdSalida();
                    kardexServicio.registrarSalida(productoBD, detalle.getCantidad(), precioUnitario, referencia, username, "");
                } catch (Exception ignored) { }
            }
        }
        // Si es venta a crédito, crear registro de crédito asociado
        if ("CREDITO".equalsIgnoreCase(nuevaSalida.getTipoVenta())) {
            Credito credito = new Credito();
            credito.setSalida(nuevaSalida);
            credito.setCliente(nuevaSalida.getCliente());
            credito.setMontoTotal(nuevaSalida.getTotalSalida());
            credito.setSaldoPendiente(nuevaSalida.getTotalSalida());
            credito.setFechaInicio(nuevaSalida.getFechaSalida());
            // fecha vencimiento: tomarla desde fechaPagoCredito si llegó; de lo contrario null
            if (nuevaSalida.getFechaPagoCredito() != null) {
                credito.setFechaVencimiento(nuevaSalida.getFechaPagoCredito());
            }
            // Nota: Salida no guarda fechaPagoCredito; si se requiere persistirla en Salida, agregar campo. Aquí solo lo dejamos null si no existe flujo.
            credito.setEstado("PENDIENTE");
            creditoRepositorio.save(credito);
        }

        return nuevaSalida;
    }

    @Override
    public List<Salida> listarSalidas() {
        return salidaRepositorio.findAllByOrderByIdSalidaDesc();
    }

    @Override
    public Page<Salida> listarSalidas(Pageable pageable) {
        return salidaRepositorio.findAll(pageable);
    }

    @Override
    @Transactional
    public Salida actualizarSalida(Long id, Salida salida) {
        return salidaRepositorio.findById(id).map(salidaExistente -> {
            salidaExistente.setFechaSalida(salida.getFechaSalida());
            salidaExistente.setCliente(salida.getCliente());
            salidaExistente.setTipoVenta(salida.getTipoVenta());
            
            // Recalcular total a partir de los detalles entrantes
            double totalCalculado = 0.0;
            if (salida.getDetalles() != null) {
                for (DetalleSalida detalle : salida.getDetalles()) {
                    double precioUnitario = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0;
                    int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                    double subtotal = cantidad * precioUnitario;
                    detalle.setSubtotal(subtotal);
                    totalCalculado += subtotal;
                }
            }
            salidaExistente.setTotalSalida(totalCalculado);

            detalleSalidaRepositorio.deleteAll(salidaExistente.getDetalles());
            if (salida.getDetalles() != null) {
                for (DetalleSalida detalle : salida.getDetalles()) {
                    detalle.setSalida(salidaExistente);
                }
            }
            salidaExistente.setDetalles(salida.getDetalles());

            return salidaRepositorio.save(salidaExistente);
        }).orElseThrow(() -> new RuntimeException("Salida no encontrada"));
    }

    @Override
    public Salida obtenerPorId(Long id) {
        return salidaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Salida no encontrada"));
    }

    @Override
    @Transactional
    public Salida cancelarSalida(Long id) {
        Salida salida = salidaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Salida no encontrada"));
        if ("Cancelado".equalsIgnoreCase(salida.getEstado())) {
            return salida; // ya cancelada
        }

        // Revertir stock global por producto (simétrico a eliminarSalida)
        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                Producto producto = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (producto != null) {
                    Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;
                    producto.setStock(stockActual + (detalle.getCantidad() == null ? 0 : detalle.getCantidad()));
                    productoRepositorio.save(producto);
                }
            }
        }

        salida.setEstado("Cancelado");
        return salidaRepositorio.save(salida);
    }

    @Override
    @Transactional
    public void eliminarSalida(Long id) {
        Salida salida = salidaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Salida no encontrada"));

        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                Producto producto = productoRepositorio.findById(detalle.getProducto().getIdProducto())
                        .orElse(null);
                if (producto != null) {
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepositorio.save(producto);
                }
            }
        }
        salidaRepositorio.deleteById(id);
    }

    @Override
    public List<Salida> filtrarPorFecha(LocalDate fecha) {
        return salidaRepositorio.findByFechaSalidaOrderByIdSalidaDesc(fecha);
    }

    @Override
    public List<Salida> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return salidaRepositorio.findByFechaSalidaBetweenOrderByIdSalidaDesc(fechaInicio, fechaFin);
    }
}


