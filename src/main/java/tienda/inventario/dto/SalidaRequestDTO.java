package tienda.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SalidaRequestDTO {
    @NotNull
    private LocalDate fechaSalida;

    @NotNull
    @Size(min = 1, message = "Debe tener al menos un detalle")
    private List<DetalleSalidaRequestDTO> detalles;
    
    @NotNull
    private ClienteReferenciaDTO cliente; // Cliente asociado
    
    @NotBlank
    private String tipoVenta; // "CONTADO" o "CREDITO"
}


