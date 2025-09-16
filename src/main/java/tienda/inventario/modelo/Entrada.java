package tienda.inventario.modelo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "entradas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "detalles")
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntrada;

    @ManyToOne
    @JoinColumn(name = "idProveedor")
    private Proveedor proveedor;

    private LocalDate fechaEntrada;

    private Double totalEntrada;

    @Column(name = "factura_url")
    private String facturaUrl; // URL de la factura subida

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones; // Notas adicionales

    @Column(name = "estado", length = 20)
    private String estado = "Registrada"; // Estado de la entrada

    @Column(name = "numero_factura", length = 50)
    private String numeroFactura; // Número de la factura/boleta

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleEntrada> detalles;
}
