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
@ToString(exclude = "categoria")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(name = "nombre_producto") // 👈 coincide con la BD
    private String nombreProducto;

    private Double precio;
    private Integer stock;

    @Column(name = "fecha_ingreso") // 👈 coincide con la BD
    private LocalDate fechaIngreso;

    @ManyToOne
    @JoinColumn(name = "id_categoria") // 👈 aquí el cambio importante
    @JsonIgnoreProperties("productos")
    private Categoria categoria;
}
