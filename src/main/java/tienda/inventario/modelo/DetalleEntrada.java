package tienda.inventario.modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "detalles_entrada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"entrada", "producto"})
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

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // Fecha de vencimiento del lote

    @OneToOne(mappedBy = "detalleEntrada", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("detalleEntrada")
    private Lote lote;
}
