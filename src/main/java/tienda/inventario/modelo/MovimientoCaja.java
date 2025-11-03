package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "movimiento_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", nullable = false)
    @NotNull(message = "La caja es obligatoria")
    @JsonIgnoreProperties({"movimientos"}) // Evita la recursión infinita
    private CajaDiaria cajaDiaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @Column(name = "monto", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @Column(name = "descripcion", length = 255)
    @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
    private String descripcion;

    @Column(name = "fecha_movimiento", nullable = false)
    @NotNull(message = "La fecha del movimiento es obligatoria")
    private LocalDateTime fechaMovimiento;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "referencia_documento", length = 100)
    private String referenciaDocumento; // Número de boleta, factura, etc.

    @Column(name = "observaciones", length = 500)
    @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
    private String observaciones;

    // Relación opcional con salida (para ventas al contado)
    @ManyToOne
    @JoinColumn(name = "id_salida")
    private Salida salida;

    // Relación opcional con entrada (para compras)
    @ManyToOne
    @JoinColumn(name = "id_entrada")
    private Entrada entrada;

    // Relación opcional con pago de crédito
    @ManyToOne
    @JoinColumn(name = "id_pago_credito")
    private PagoCredito pagoCredito;

    @PrePersist
    public void prePersist() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }

    // Enum para los tipos de movimiento
    public enum TipoMovimiento {
        INGRESO,    // Venta al contado, pago de crédito, etc.
        EGRESO      // Compra, gasto, retiro, etc.
    }
}




