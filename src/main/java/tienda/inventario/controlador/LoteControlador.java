package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Lote;
import tienda.inventario.repositorio.LoteRepositorio;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteControlador {

    @Autowired
    private LoteRepositorio loteRepositorio;

    // Obtener lotes por producto
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<Lote>> obtenerLotesPorProducto(@PathVariable Long idProducto) {
        List<Lote> lotes = loteRepositorio.findByProductoIdProducto(idProducto);
        return ResponseEntity.ok(lotes);
    }

    // Obtener lotes próximos a vencer (próximos 30 días)
    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<Lote>> obtenerLotesProximosAVencer() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusDays(30);
        List<Lote> lotes = loteRepositorio.findLotesProximosAVencer(fechaActual, fechaLimite);
        return ResponseEntity.ok(lotes);
    }

    // Obtener lotes vencidos
    @GetMapping("/vencidos")
    public ResponseEntity<List<Lote>> obtenerLotesVencidos() {
        List<Lote> lotes = loteRepositorio.findLotesVencidos(LocalDate.now());
        return ResponseEntity.ok(lotes);
    }

    // Obtener stock total de un producto
    @GetMapping("/producto/{idProducto}/stock")
    public ResponseEntity<Integer> obtenerStockTotalPorProducto(@PathVariable Long idProducto) {
        Integer stockTotal = loteRepositorio.getStockTotalPorProducto(idProducto);
        return ResponseEntity.ok(stockTotal);
    }

    // Obtener resumen de alertas
    @GetMapping("/alertas/resumen")
    public ResponseEntity<AlertaResumenDTO> obtenerResumenAlertas() {
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusDays(30);
        
        List<Lote> lotesVencidos = loteRepositorio.findLotesVencidos(fechaActual);
        List<Lote> lotesProximos = loteRepositorio.findLotesProximosAVencer(fechaActual, fechaLimite);
        
        AlertaResumenDTO resumen = new AlertaResumenDTO();
        resumen.setTotalAlertas(lotesVencidos.size() + lotesProximos.size());
        resumen.setLotesVencidos(lotesVencidos.size());
        resumen.setLotesProximosAVencer(lotesProximos.size());
        
        return ResponseEntity.ok(resumen);
    }

    // Clase interna para el resumen
    public static class AlertaResumenDTO {
        private int totalAlertas;
        private int lotesVencidos;
        private int lotesProximosAVencer;

        // Getters y setters
        public int getTotalAlertas() { return totalAlertas; }
        public void setTotalAlertas(int totalAlertas) { this.totalAlertas = totalAlertas; }
        
        public int getLotesVencidos() { return lotesVencidos; }
        public void setLotesVencidos(int lotesVencidos) { this.lotesVencidos = lotesVencidos; }
        
        public int getLotesProximosAVencer() { return lotesProximosAVencer; }
        public void setLotesProximosAVencer(int lotesProximosAVencer) { this.lotesProximosAVencer = lotesProximosAVencer; }
    }
}
