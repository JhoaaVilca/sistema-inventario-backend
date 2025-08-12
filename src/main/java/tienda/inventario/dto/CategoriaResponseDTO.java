package tienda.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long idCategoria;
    private String nombre;
}
