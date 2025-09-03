package tienda.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DetalleSalidaRequestDTO {
    @NotNull
    private ProductoReferenciaDTO producto;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    private Double precioUnitario;
}


