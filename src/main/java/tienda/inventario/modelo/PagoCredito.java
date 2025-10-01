package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "pagos_credito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @ManyToOne
    @JoinColumn(name = "id_credito")
    private Credito credito;

    private LocalDate fechaPago;

    private Double monto;

    @Column(length = 30)
    private String medioPago; // EFECTIVO, TRANSFERENCIA, etc.

    @Column(length = 255)
    private String observacion;
}


