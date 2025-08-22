package tienda.inventario.modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_salida")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"salida", "producto"})
public class DetalleSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "idSalida")
    @JsonBackReference
    private Salida salida;

    @ManyToOne
    @JoinColumn(name = "idProducto")
    private Producto producto;

    private Integer cantidad;

    private Double precioUnitario;

    private Double subtotal;
}


