package tienda.inventario.mapper;

import tienda.inventario.dto.CategoriaRequestDTO;
import tienda.inventario.dto.CategoriaResponseDTO;
import tienda.inventario.modelo.Categoria;

public class CategoriaMapper {

    public static Categoria toEntity(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return categoria;
    }

    public static CategoriaResponseDTO toResponse(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.isActivo()
        );
    }
}
