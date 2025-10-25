package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "caja_diaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "movimientos")
public class CajaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCaja;

    @Column(name = "fecha", nullable = false)
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @Column(name = "monto_apertura", precision = 10, scale = 2)
    @NotNull(message = "El monto de apertura es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto de apertura debe ser mayor o igual a 0")
    private BigDecimal montoApertura;

    @Column(name = "total_ingresos", precision = 10, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "total_egresos", precision = 10, scale = 2)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Column(name = "monto_cierre", precision = 10, scale = 2)
    private BigDecimal montoCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @NotNull(message = "El estado es obligatorio")
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "usuario_apertura", length = 100)
    private String usuarioApertura;

    @Column(name = "usuario_cierre", length = 100)
    private String usuarioCierre;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // Relación con movimientos de caja
    @OneToMany(mappedBy = "cajaDiaria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cajaDiaria") // Evita la recursión infinita
    private List<MovimientoCaja> movimientos;

    // Método para calcular el saldo actual
    public BigDecimal getSaldoActual() {
        if (montoApertura == null) return BigDecimal.ZERO;
        return montoApertura.add(totalIngresos != null ? totalIngresos : BigDecimal.ZERO)
                           .subtract(totalEgresos != null ? totalEgresos : BigDecimal.ZERO);
    }

    // Método para verificar si la caja está abierta
    public boolean isAbierta() {
        return EstadoCaja.ABIERTA.equals(this.estado);
    }

    // Método para verificar si la caja está cerrada
    public boolean isCerrada() {
        return EstadoCaja.CERRADA.equals(this.estado);
    }

    // Enum para los estados de la caja
    public enum EstadoCaja {
        ABIERTA, CERRADA
    }
}

