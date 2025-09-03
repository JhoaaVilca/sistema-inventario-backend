package tienda.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SalidaRequestDTO {
    @NotNull
    @PastOrPresent
    private LocalDate fechaSalida;

    @NotEmpty
    private List<DetalleSalidaRequestDTO> detalles;
    
    private ClienteReferenciaDTO cliente; // Cliente asociado
    private String tipoVenta; // "CONTADO" o "CREDITO"
}


