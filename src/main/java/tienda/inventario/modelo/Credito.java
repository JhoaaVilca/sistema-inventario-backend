package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "creditos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"pagos"})
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCredito;

    @OneToOne
    @JoinColumn(name = "id_salida", unique = true)
    private Salida salida;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    private Double montoTotal;

    private Double saldoPendiente;

    private LocalDate fechaInicio;

    private LocalDate fechaVencimiento;

    @Column(length = 20)
    private String estado; // PENDIENTE, PARCIAL, CANCELADO, VENCIDO

    @Column(length = 255)
    private String observacion;

    @OneToMany(mappedBy = "credito", cascade = CascadeType.ALL)
    private List<PagoCredito> pagos;
}


