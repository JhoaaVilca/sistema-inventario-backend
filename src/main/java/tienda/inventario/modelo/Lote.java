package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"detalleEntrada"})
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLote;

    @Column(name = "numero_lote", length = 50)
    private String numeroLote; // Ej: "LOTE-2024-01-15"

    @OneToOne
    @JoinColumn(name = "id_detalle_entrada")
    @JsonIgnoreProperties("lote")
    private DetalleEntrada detalleEntrada;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "estado", length = 20)
    private String estado = "Activo"; // Activo, Vencido, Agotado

    // Método para verificar si está próximo a vencer (30 días)
    public boolean estaProximoAVencer() {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.isBefore(LocalDate.now().plusDays(30)) 
               && fechaVencimiento.isAfter(LocalDate.now());
    }

    // Método para verificar si está vencido
    public boolean estaVencido() {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    // Método para calcular días hasta vencimiento
    public long diasHastaVencimiento() {
        if (fechaVencimiento == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }
}
