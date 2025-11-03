package tienda.inventario.dto;

import java.util.List;

public class DashboardResumenDTO {
    public long productosTotales;
    public long proveedoresTotales;
    public long entradasTotales;
    public double ventasTotales; // suma del mes
    public long ventasCantidadMes; // cantidad de salidas del mes
    public long productosSinStock;
    public long creditosPendientes;
    public long productosPorVencer;
    public long categoriasTotales;

    public List<DashboardActividadDTO> actividadReciente;
    public List<DashboardMensualDTO> resumenMensual;
    public List<DashboardAlertaDTO> alertas;
}


