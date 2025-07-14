package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.ProductoRepositorio;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicio implements IProductoServicio {

    @Autowired
    private ProductoRepositorio repositorio;

    @Override
    public List<Producto> listarProductos() {
        return repositorio.findAll();
    }

    @Override
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return repositorio.findById(id);
    }

    @Override
    public Producto guardarProducto(Producto producto) {
        return repositorio.save(producto);
    }

    @Override
    public void eliminarProducto(Long id) {
        repositorio.deleteById(id);
    }
}
