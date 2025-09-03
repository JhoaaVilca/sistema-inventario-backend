package tienda.inventario.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long idCliente;
    private String dni;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String telefono;
    private String email;
    private LocalDateTime fechaRegistro;
    private Boolean activo;
}
