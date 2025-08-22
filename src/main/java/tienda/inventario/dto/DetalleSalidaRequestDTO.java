package tienda.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DetalleSalidaRequestDTO {
    @NotNull
    private Long idProducto;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    private Double precioUnitario;
}


