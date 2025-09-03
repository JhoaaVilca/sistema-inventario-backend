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

    @Override
    @Transactional
    public Salida guardarSalida(Salida salida) {
        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                detalle.setSalida(salida);
            }
        }

        // Validación de stock y cálculo de subtotales/total antes de guardar
        double totalCalculado = 0.0;
        if (salida.getDetalles() != null) {
            for (DetalleSalida detalle : salida.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD == null) {
                    throw new RuntimeException("Producto no encontrado: id=" + detalle.getProducto().getIdProducto());
                }
                int stockActual = productoBD.getStock() != null ? productoBD.getStock() : 0;
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                if (cantidad <= 0) {
                    throw new RuntimeException("Cantidad inválida para el producto id=" + productoBD.getIdProducto());
                }
                if (stockActual < cantidad) {
                    throw new RuntimeException("Stock insuficiente para el producto '" + productoBD.getNombreProducto() + "' (stock=" + stockActual + ", solicitado=" + cantidad + ")");
                }

                double precioUnitario = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : 0.0;
                double subtotal = cantidad * precioUnitario;
                detalle.setSubtotal(subtotal);
                totalCalculado += subtotal;
            }
        }
        salida.setTotalSalida(totalCalculado);

        Salida nuevaSalida = salidaRepositorio.save(salida);

        if (nuevaSalida.getDetalles() != null) {
            for (DetalleSalida detalle : nuevaSalida.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD != null) {
                    Integer stockActual = productoBD.getStock() != null ? productoBD.getStock() : 0;
                    productoBD.setStock(stockActual - detalle.getCantidad());
                    productoRepositorio.save(productoBD);
                }
            }
        }
        return nuevaSalida;
    }

    @Override
    public List<Salida> listarSalidas() {
        return salidaRepositorio.findAllByOrderByIdSalidaDesc();
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


