package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.modelo.DetalleEntrada;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.DetalleEntradaRepositorio;
import tienda.inventario.repositorio.EntradaRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;

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
        // Establecer la relación bidireccional solo si la lista no es nula
        if (entrada.getDetalles() != null) {
            for (DetalleEntrada detalle : entrada.getDetalles()) {
                detalle.setEntrada(entrada);
            }
        }
        // Guardar la entrada y sus detalles en cascada
        Entrada nuevaEntrada = entradaRepositorio.save(entrada);

        // Actualizar el stock de los productos solo si la lista no es nula
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
}
