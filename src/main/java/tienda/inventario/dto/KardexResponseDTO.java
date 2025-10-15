package tienda.inventario.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class KardexResponseDTO {
    private Long idKardex;
    private Long idProducto;
    private String nombreProducto;
    private LocalDateTime fechaMovimiento;
    private String tipoMovimiento;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal valorTotal;
    private Integer stockAnterior;
    private Integer stockActual;
    private BigDecimal costoPromedioAnterior;
    private BigDecimal costoPromedioActual;
    private String observaciones;
    private String referenciaDocumento;
    private String usuarioRegistro;
}

