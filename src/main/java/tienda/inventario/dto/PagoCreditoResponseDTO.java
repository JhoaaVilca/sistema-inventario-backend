package tienda.inventario.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PagoCreditoResponseDTO {
    private Long idPago;
    private LocalDate fechaPago;
    private Double monto;
    private String medioPago;
    private String observacion;
}


