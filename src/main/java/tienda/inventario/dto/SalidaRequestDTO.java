package tienda.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SalidaRequestDTO {
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;

    @NotNull
    @Size(min = 1, message = "Debe tener al menos un detalle")
    private List<DetalleSalidaRequestDTO> detalles;
    
    @NotNull
    private ClienteReferenciaDTO cliente; // Cliente asociado
    
    @NotBlank
    private String tipoVenta; // "CONTADO" o "CREDITO"

    // Total de la salida. Si no viene, el backend puede calcularlo a partir de detalles
    @Positive
    private Double totalSalida;

    // Solo para ventas a crédito: fecha comprometida de pago
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPagoCredito;
}


