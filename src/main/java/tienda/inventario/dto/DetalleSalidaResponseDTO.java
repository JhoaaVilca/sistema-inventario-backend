package tienda.inventario.dto;

import lombok.Data;

@Data
public class DetalleSalidaResponseDTO {
    private Long idDetalle;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}


