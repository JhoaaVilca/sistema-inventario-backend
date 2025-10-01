package tienda.inventario.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreditoResponseDTO {
    private Long idCredito;
    private Long idSalida;
    private Long idCliente;
    private String nombreCliente;
    private Double montoTotal;
    private Double saldoPendiente;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private String estado;
    private String observacion;
    private List<PagoCreditoResponseDTO> pagos;
}


