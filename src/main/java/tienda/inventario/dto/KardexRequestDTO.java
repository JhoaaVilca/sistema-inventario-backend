package tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KardexRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento; // ENTRADA, SALIDA, AJUSTE

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private BigDecimal precioUnitario; // Opcional para ajustes de salida

    private String observaciones;

    private String usuarioRegistro; // Usuario que realiza el ajuste
}

