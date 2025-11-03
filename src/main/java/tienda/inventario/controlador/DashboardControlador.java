package tienda.inventario.controlador;

import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.*;
import tienda.inventario.servicios.DashboardServicio;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControlador {

    private final DashboardServicio servicio;

    public DashboardControlador(DashboardServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public DashboardResumenDTO resumen() {
        return servicio.obtenerResumen();
    }

    @GetMapping("/actividad-reciente")
    public List<DashboardActividadDTO> actividad(@RequestParam(defaultValue = "10") int limite) {
        return servicio.actividadReciente(limite);
    }

    @GetMapping("/alertas")
    public List<DashboardAlertaDTO> alertas() {
        return servicio.alertas();
    }

    @GetMapping("/proximos-vencer")
    public List<ProductoPorVencerDTO> proximosVencer() {
        return servicio.proximosAVencer();
    }

    @GetMapping("/deudores")
    public List<DeudorDTO> deudores(@RequestParam(defaultValue = "5") int limite) {
        return servicio.deudoresTop(limite);
    }

    @GetMapping("/vencimientos")
    public VencimientosDTO vencimientos() {
        return servicio.listarVencimientos();
    }
}


