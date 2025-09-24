package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Lote;
import tienda.inventario.repositorio.LoteRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3001")
@RequestMapping("/api/lotes")
public class LoteControlador {

    @Autowired
    private LoteRepositorio loteRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

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

    // Dar de baja un lote (marca estado distinto de 'Activo' para excluir de stock)
    @PostMapping("/{id}/baja")
    @Transactional
    public ResponseEntity<?> darDeBaja(@PathVariable("id") Long idLote, @RequestBody(required = false) BajaRequest request) {
        Optional<Lote> opt = loteRepositorio.findById(idLote);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Lote lote = opt.get();
        String estadoAnterior = lote.getEstado();
        // Marcar como "Baja" (puedes usar "Inactivo" si prefieres)
        lote.setEstado("Baja");
        loteRepositorio.save(lote);
        try { loteRepositorio.flush(); } catch (Exception ignore) {}

        // Ajustar stock del producto solo si el lote estaba Activo antes
        try {
            if ("Activo".equalsIgnoreCase(estadoAnterior)) {
                var detalle = lote.getDetalleEntrada();
                if (detalle != null && detalle.getProducto() != null && detalle.getCantidad() != null) {
                    var producto = detalle.getProducto();
                    Integer actual = producto.getStock() == null ? 0 : producto.getStock();
                    int nueva = Math.max(0, actual - detalle.getCantidad());
                    producto.setStock(nueva);
                    productoRepositorio.save(producto);
                }
            }
        } catch (Exception e) {
            // Loguear pero no romper la respuesta
            System.err.println("Error ajustando stock al dar de baja lote: " + e.getMessage());
        }

        // Nota: Si tienes entidad de Merma/Auditoría, aquí puedes registrar (motivo/observación)
        // usando request.getMotivo() y request.getObservacion().

        return ResponseEntity.ok().build();
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

    // DTO para recibir motivo/observación de la baja
    public static class BajaRequest {
        private String motivo;
        private String observacion;

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
        public String getObservacion() { return observacion; }
        public void setObservacion(String observacion) { this.observacion = observacion; }
    }
}
