package tienda.inventario.dto;

import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;
    private Boolean esPerecible = false; // Indica si el producto vence
    private String descripcionCorta; // Descripción opcional
    private Long idCategoria; // 👈 pasamos solo el id de la categoría
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimientoInicial; // Opcional: requerido si es perecible y hay stock inicial
}
