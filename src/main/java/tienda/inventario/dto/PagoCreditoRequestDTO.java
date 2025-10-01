package tienda.inventario.dto;

import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class PagoCreditoRequestDTO {
    private Long idCredito;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
    private Double monto;
    private String medioPago;
    private String observacion;
}


