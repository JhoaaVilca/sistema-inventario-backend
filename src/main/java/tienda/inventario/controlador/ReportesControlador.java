package tienda.inventario.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tienda.inventario.dto.DashboardMensualDTO;
import tienda.inventario.servicios.DashboardServicio;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportesControlador {

    private final DashboardServicio dashboardServicio;

    public ReportesControlador(DashboardServicio dashboardServicio) {
        this.dashboardServicio = dashboardServicio;
    }

    @GetMapping("/mensual")
    public List<DashboardMensualDTO> mensual() {
        return dashboardServicio.resumenMensual();
    }
}


