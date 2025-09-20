package tienda.inventario.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
    private String nombreProducto;
    private Double precio; // Precio de venta
    private Double precioCompra; // Precio de compra
    private Integer stock;
    private Integer stockMinimo; // Stock mínimo para alertas
    private String unidadMedida; // unidad, kg, litro, etc.
    private LocalDate fechaIngreso;
    private Boolean esPerecible = false; // Indica si el producto vence
    private String descripcionCorta; // Descripción opcional
    private Long idCategoria; // 👈 pasamos solo el id de la categoría
}
