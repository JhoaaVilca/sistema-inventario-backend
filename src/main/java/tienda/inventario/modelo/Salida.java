package tienda.inventario.modelo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import tienda.inventario.modelo.Cliente;

@Entity
@Table(name = "salidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "detalles")
public class Salida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSalida;

    private LocalDate fechaSalida;

    private Double totalSalida;

    @OneToMany(mappedBy = "salida", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleSalida> detalles;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    private String tipoVenta; // "CONTADO" o "CREDITO"

    // Para ventas a crédito: fecha comprometida de pago
    private java.time.LocalDate fechaPagoCredito;
}


