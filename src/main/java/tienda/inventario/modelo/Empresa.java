package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpresa;

    @Column(nullable = false, length = 200)
    private String nombreEmpresa;

    @Column(length = 20)
    private String ruc;

    @Column(length = 500)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String descripcion;

    // Constructor para inicializar con datos por defecto
    public Empresa(String nombreEmpresa, String ruc, String direccion) {
        this.nombreEmpresa = nombreEmpresa;
        this.ruc = ruc;
        this.direccion = direccion;
    }
}



