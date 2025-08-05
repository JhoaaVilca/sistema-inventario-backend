package tienda.inventario.modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_entrada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DetalleEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "idEntrada")
    @JsonBackReference
    private Entrada entrada;

    @ManyToOne
    @JoinColumn(name = "idProducto")
    private Producto producto;

    private Integer cantidad;

    private Double precioUnitario;

    private Double subtotal;
}
