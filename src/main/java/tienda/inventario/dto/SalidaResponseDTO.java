package tienda.inventario.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalidaResponseDTO {
    private Long idSalida;
    private LocalDate fechaSalida;
    private Double totalSalida;
    private List<DetalleSalidaResponseDTO> detalles;
}


