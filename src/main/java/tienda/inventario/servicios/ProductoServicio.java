package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.ProductoRepositorio;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
        // Verificar si el producto existe antes de eliminar
        if (!repositorio.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + id);
        }
        
        try {
            repositorio.deleteById(id);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            
            // Detectar restricciones específicas de foreign key
            if (errorMessage != null && errorMessage.contains("FKf1rbsyrsesw6iujxcem9nxvrj")) {
                throw new RuntimeException("No se puede eliminar el producto porque está siendo usado en detalles de entrada. " +
                    "Primero debe eliminar todos los registros relacionados en la tabla 'detalles_entrada'.");
            }
            
            if (errorMessage != null && errorMessage.contains("REFERENCE")) {
                throw new RuntimeException("No se puede eliminar el producto porque está siendo referenciado por otras entidades. " +
                    "Verifique que no haya registros relacionados en otras tablas.");
            }
            
            // Error genérico
            throw new RuntimeException("No se puede eliminar el producto. Puede estar relacionado con otras entidades o tener restricciones de base de datos.", e);
        }
    }
}
