package tienda.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequestDTO {
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
