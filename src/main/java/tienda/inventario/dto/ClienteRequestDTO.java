package tienda.inventario.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {
    private String dni;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String email;
}
