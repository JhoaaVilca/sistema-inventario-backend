package tienda.inventario.dto;

import java.time.LocalDate;

public class ProductoPorVencerDTO {
    public Long idProducto;
    public String nombreProducto;
    public LocalDate fechaVencimiento;
    public int diasRestantes;
}


