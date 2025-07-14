package tienda.inventario.servicios;

import tienda.inventario.modelo.Producto;
import java.util.List;
import java.util.Optional;

public interface IProductoServicio {

    public List<Producto> listarProductos();
    public Optional<Producto> obtenerProductoPorId(Long id);
    public Producto guardarProducto(Producto producto);
    public void eliminarProducto(Long id);
}
