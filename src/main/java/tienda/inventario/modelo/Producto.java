package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"categoria"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(name = "nombre_producto") // 👈 coincide con la BD
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre del producto no debe exceder 150 caracteres")
    private String nombreProducto;

    @Column(name = "precio_venta")
    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private Double precio; // Precio de venta

    @Column(name = "precio_compra")
    @NotNull(message = "El precio de compra es obligatorio")
    @Positive(message = "El precio de compra debe ser mayor a 0")
    private Double precioCompra; // Precio de compra para calcular margen

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Column(name = "stock_minimo")
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo; // Stock mínimo para alertas

    @Column(name = "unidad_medida", length = 20)
    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 20, message = "La unidad de medida no debe exceder 20 caracteres")
    private String unidadMedida; // unidad, kg, litro, etc.

    @Column(name = "fecha_ingreso") // 👈 coincide con la BD
    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    @Column(name = "es_perecible")
    private Boolean esPerecible = false; // Indica si el producto vence

    @Column(name = "descripcion_corta", length = 255)
    @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
    private String descripcionCorta; // Descripción opcional

    @ManyToOne
    @JoinColumn(name = "id_categoria") // 👈 aquí el cambio importante
    @JsonIgnoreProperties("productos")
    @NotNull(message = "La categoría es obligatoria")
    private Categoria categoria;

}
