package tienda.inventario.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Long idProducto;
    private String nombreProducto;
    private Double precio;
    private Integer stock;
    private LocalDate fechaIngreso;
    private Long idCategoria;
    private String nombreCategoria; // 👈 listo para mostrar en el front
}
