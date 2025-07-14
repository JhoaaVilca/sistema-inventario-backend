package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    private String nombreProducto;

    private Double precio;

    private Integer stock;

    private String categoria;

    @Temporal(TemporalType.DATE)
    private Date fechaIngreso;
}
