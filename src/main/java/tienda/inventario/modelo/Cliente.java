package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(name = "dni", unique = true, nullable = false)
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 12, message = "El documento debe tener entre 8 y 12 caracteres")
    private String dni;

    @Column(name = "nombres", nullable = false)
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 150, message = "Los nombres no deben exceder 150 caracteres")
    private String nombres;

    @Column(name = "apellidos")
    @Size(max = 150, message = "Los apellidos no deben exceder 150 caracteres")
    private String apellidos;

    @Column(name = "direccion")
    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    private String direccion;

    @Column(name = "telefono")
    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    private String telefono;

    @Column(name = "email")
    @Email(message = "El email no es válido")
    @Size(max = 150, message = "El email no debe exceder 150 caracteres")
    private String email;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
