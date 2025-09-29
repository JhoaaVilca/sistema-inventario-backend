package tienda.inventario.servicios;

import tienda.inventario.modelo.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICategoriaServicio {
    List<Categoria> listarCategorias();
    Page<Categoria> listarCategorias(Pageable pageable);
    Optional<Categoria> obtenerCategoriaPorId(Long id);
    Categoria guardarCategoria(Categoria categoria);
    void eliminarCategoria(Long id);
}
