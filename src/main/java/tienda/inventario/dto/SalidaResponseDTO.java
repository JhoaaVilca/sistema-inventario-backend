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
    
    // Información del cliente
    private Long idCliente;
    private String dniCliente;
    private String nombreCliente;
    private String tipoVenta;
    // Para ventas a crédito
    private LocalDate fechaPagoCredito;
    // Estado de la salida
    private String estado;
}


