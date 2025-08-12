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

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<DetalleEntrada> detalles;
}
