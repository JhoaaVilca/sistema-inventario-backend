package tienda.inventario.dto;

import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class CreditoRequestDTO {
    private Long idSalida;
    private Long idCliente;
    private Double montoTotal;
    private Double saldoPendiente;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimiento;
    private String observacion;
}


