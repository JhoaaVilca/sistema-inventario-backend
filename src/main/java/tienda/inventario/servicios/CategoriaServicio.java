package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.repositorio.CategoriaRepositorio;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaServicio implements ICategoriaServicio {

    @Autowired
    private CategoriaRepositorio repositorio;

    @Override
    public List<Categoria> listarCategorias() {
        return repositorio.findAll();
    }

    @Override
    public Page<Categoria> listarCategorias(Pageable pageable) {
        return repositorio.findAll(pageable);
    }

    @Override
    public Optional<Categoria> obtenerCategoriaPorId(Long id) {
        return repositorio.findById(id);
    }

    @Override
    public Categoria guardarCategoria(Categoria categoria) {
        return repositorio.save(categoria);
    }

    @Override
    public void eliminarCategoria(Long id) {
        // Verificar si la categoría existe antes de eliminar
        if (!repositorio.existsById(id)) {
            throw new IllegalArgumentException("Categoría no encontrada con ID: " + id);
        }
        
        try {
            repositorio.deleteById(id);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            
            // Detectar restricciones específicas de foreign key
            if (errorMessage != null && errorMessage.contains("REFERENCE")) {
                throw new RuntimeException("No se puede eliminar la categoría porque está siendo referenciada por productos. " +
                    "Primero debe eliminar o cambiar la categoría de todos los productos relacionados.");
            }
            
            // Error genérico
            throw new RuntimeException("No se puede eliminar la categoría. Puede estar relacionada con productos o tener restricciones de base de datos.", e);
        }
    }
}
