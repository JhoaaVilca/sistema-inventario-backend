package tienda.inventario.mapper;

import tienda.inventario.dto.ProductoRequestDTO;
import tienda.inventario.dto.ProductoResponseDTO;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.modelo.Producto;

public class ProductoMapper {

    public static Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        Producto p = new Producto();
        p.setNombreProducto(dto.getNombreProducto());
        p.setPrecio(dto.getPrecio());
        p.setStock(dto.getStock());
        p.setFechaIngreso(dto.getFechaIngreso());
        p.setCategoria(categoria);
        return p;
    }

    public static ProductoResponseDTO toResponse(Producto p) {
        return new ProductoResponseDTO(
                p.getIdProducto(),
                p.getNombreProducto(),
                p.getPrecio(),
                p.getStock(),
                p.getFechaIngreso(),
                p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null
        );
    }
}
