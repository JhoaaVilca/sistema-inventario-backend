package tienda.inventario.dto;

import java.time.LocalDate;

public class DashboardActividadDTO {
    public LocalDate fecha;
    public String tipo; // Venta | Entrada | Pago Crédito
    public String detalle;
    public String usuario;
    public Double monto;
}


