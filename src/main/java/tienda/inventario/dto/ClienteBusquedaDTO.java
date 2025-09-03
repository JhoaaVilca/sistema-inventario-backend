package tienda.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteBusquedaDTO {
    private String dni;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String email;
    private Boolean existeEnBD;
    private String mensaje;
}
