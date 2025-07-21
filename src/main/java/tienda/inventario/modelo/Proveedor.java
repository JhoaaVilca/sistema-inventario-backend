package tienda.inventario.modelo;

import jakarta.persistence.*;
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

    private String nombre;

    private String tipoDocumento; // "RUC" o "DNI"

    private String numeroDocumento; // el número del RUC o DNI

    private String direccion;

    private String telefono;

    private String email;
}
