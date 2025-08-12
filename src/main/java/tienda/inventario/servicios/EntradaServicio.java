package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.modelo.DetalleEntrada;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.DetalleEntradaRepositorio;
import tienda.inventario.repositorio.EntradaRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;

import java.time.LocalDate;
import java.util.List;

@Service
public class EntradaServicio implements IEntradaServicio {

    @Autowired
    private EntradaRepositorio entradaRepositorio;

    @Autowired
    private DetalleEntradaRepositorio detalleRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Override
    public Entrada guardarEntrada(Entrada entrada) {
        if (entrada.getDetalles() != null) {
            for (DetalleEntrada detalle : entrada.getDetalles()) {
                detalle.setEntrada(entrada);
            }
        }
        Entrada nuevaEntrada = entradaRepositorio.save(entrada);

        if (nuevaEntrada.getDetalles() != null) {
            for (DetalleEntrada detalle : nuevaEntrada.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD != null) {
                    Integer stockActual = productoBD.getStock() != null ? productoBD.getStock() : 0;
                    productoBD.setStock(stockActual + detalle.getCantidad());
                    productoRepositorio.save(productoBD);
                }
            }
        }
        return nuevaEntrada;
    }

    @Override
    public List<Entrada> listarEntradas() {
        return entradaRepositorio.findAll();
    }

    @Override
    public Entrada actualizarEntrada(Long id, Entrada entrada) {
        return entradaRepositorio.findById(id).map(entradaExistente -> {
            entradaExistente.setProveedor(entrada.getProveedor());
            entradaExistente.setFechaEntrada(entrada.getFechaEntrada());
            entradaExistente.setTotalEntrada(entrada.getTotalEntrada());

            detalleRepositorio.deleteAll(entradaExistente.getDetalles());
            if (entrada.getDetalles() != null) {
                for (DetalleEntrada detalle : entrada.getDetalles()) {
                    detalle.setEntrada(entradaExistente);
                }
            }
            entradaExistente.setDetalles(entrada.getDetalles());

            return entradaRepositorio.save(entradaExistente);
        }).orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
    }

    @Override
    public void eliminarEntrada(Long id) {
        Entrada entrada = entradaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));

        if (entrada.getDetalles() != null) {
            for (DetalleEntrada detalle : entrada.getDetalles()) {
                Producto producto = productoRepositorio.findById(detalle.getProducto().getIdProducto())
                        .orElse(null);
                if (producto != null) {
                    producto.setStock(producto.getStock() - detalle.getCantidad());
                    productoRepositorio.save(producto);
                }
            }
        }
        entradaRepositorio.deleteById(id);
    }

    @Override
    public List<Entrada> filtrarPorProveedor(Long idProveedor) {
        return entradaRepositorio.findByProveedorId(idProveedor);
    }

    @Override
    public List<Entrada> filtrarPorFecha(LocalDate fecha) {
        return entradaRepositorio.findByFechaEntrada(fecha);
    }

    @Override
    public List<Entrada> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return entradaRepositorio.findByFechaEntradaBetween(fechaInicio, fechaFin);
    }
}
