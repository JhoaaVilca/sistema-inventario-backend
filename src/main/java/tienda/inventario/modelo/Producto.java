package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"categoria", "proveedorPrincipal"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(name = "nombre_producto") // 👈 coincide con la BD
    private String nombreProducto;

    @Column(name = "precio_venta")
    private Double precio; // Precio de venta

    @Column(name = "precio_compra")
    private Double precioCompra; // Precio de compra para calcular margen

    private Integer stock;

    @Column(name = "stock_minimo")
    private Integer stockMinimo; // Stock mínimo para alertas

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida; // unidad, kg, litro, etc.

    @Column(name = "fecha_ingreso") // 👈 coincide con la BD
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // Para productos perecibles

    @Column(name = "es_perecible")
    private Boolean esPerecible = false; // Indica si el producto vence

    @Column(name = "descripcion_corta", length = 255)
    private String descripcionCorta; // Descripción opcional

    @ManyToOne
    @JoinColumn(name = "id_categoria") // 👈 aquí el cambio importante
    @JsonIgnoreProperties("productos")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_proveedor_principal")
    @JsonIgnoreProperties({"productos"})
    private Proveedor proveedorPrincipal; // Proveedor principal del producto
}
