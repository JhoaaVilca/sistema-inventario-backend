package tienda.inventario.servicios;

import tienda.inventario.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IProductoServicio {

    public Page<Producto> listarProductos(Pageable pageable);
    public List<Producto> listarProductos();
    public Optional<Producto> obtenerProductoPorId(Long id);
    public Producto guardarProducto(Producto producto);
    public void eliminarProducto(Long id);
    public List<Producto> buscarProductosPorNombre(String nombre);
}
