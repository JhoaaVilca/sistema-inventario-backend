package tienda.inventario.servicios;

import tienda.inventario.modelo.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICategoriaServicio {
    List<Categoria> listarCategorias();
    Optional<Categoria> obtenerCategoriaPorId(Long id);
    Categoria guardarCategoria(Categoria categoria);
    void eliminarCategoria(Long id);
}
