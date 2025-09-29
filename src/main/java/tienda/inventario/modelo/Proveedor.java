package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento; // "RUC" o "DNI"

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 15, message = "El número de documento no debe exceder 15 caracteres")
    private String numeroDocumento; // el número del RUC o DNI

    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    private String telefono;

    @Email(message = "El email no es válido")
    @Size(max = 150, message = "El email no debe exceder 150 caracteres")
    private String email;

    @Column(nullable = false)  // <- obliga que no sea nulo
    private boolean activo = true; // <- valor por defecto true
}
