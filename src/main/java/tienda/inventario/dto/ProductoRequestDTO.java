package tienda.inventario.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
    private String nombreProducto;
    private Double precio;
    private Integer stock;
    private LocalDate fechaIngreso;
    private Long idCategoria; // 👈 pasamos solo el id de la categoría
}
