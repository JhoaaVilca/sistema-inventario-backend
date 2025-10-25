package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import tienda.inventario.modelo.CajaDiaria;
import tienda.inventario.modelo.MovimientoCaja;
import tienda.inventario.servicios.ICajaDiariaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/caja")
@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001"}, 
           allowedHeaders = "*", 
           allowCredentials = "true",
           exposedHeaders = "Authorization")
public class CajaDiariaControlador {

    private static final Logger logger = LoggerFactory.getLogger(CajaDiariaControlador.class);

    @Autowired
    private ICajaDiariaServicio cajaServicio;

    /**
     * Abrir caja del día
     */
    @PostMapping("/abrir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> abrirCaja(@Valid @RequestBody Map<String, Object> request) {
        try {
            BigDecimal montoApertura = new BigDecimal(request.get("montoApertura").toString());
            String usuario = request.get("usuario").toString();
            String observaciones = request.getOrDefault("observaciones", "").toString();

            CajaDiaria caja = cajaServicio.abrirCaja(montoApertura, usuario, observaciones);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caja abierta correctamente",
                "caja", caja
            ));
        } catch (Exception e) {
            logger.error("Error al abrir caja", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Cerrar caja del día
     */
    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cerrarCaja(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String usuario = request.get("usuario").toString();
            String observaciones = request.getOrDefault("observaciones", "").toString();

            CajaDiaria caja = cajaServicio.cerrarCaja(id, usuario, observaciones);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caja cerrada correctamente",
                "caja", caja
            ));
        } catch (Exception e) {
            logger.error("Error al cerrar caja", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtener estado de caja actual
     */
    /**
     * Obtener movimientos de una caja específica
     */
    @GetMapping("/{id}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> obtenerMovimientosDeCaja(@PathVariable Long id) {
        try {
            List<MovimientoCaja> movimientos = cajaServicio.obtenerMovimientosPorCaja(id);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            logger.error("Error al obtener movimientos de la caja", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> obtenerEstadoCaja() {
        try {
            Optional<CajaDiaria> cajaAbierta = cajaServicio.obtenerCajaAbierta();
            
            if (cajaAbierta.isPresent()) {
                CajaDiaria caja = cajaAbierta.get();
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "existeCaja", true,
                    "caja", caja,
                    "saldoActual", caja.getSaldoActual(),
                    "message", "Caja abierta encontrada"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "existeCaja", false,
                    "caja", null,
                    "message", "No hay caja abierta actualmente"
                ));
            }
        } catch (Exception e) {
            logger.error("Error al obtener estado de caja", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtener resumen de caja
     */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<?> obtenerResumenCaja(@PathVariable Long id) {
        try {
            CajaDiaria caja = cajaServicio.obtenerResumenCaja(id);
            List<MovimientoCaja> movimientos = cajaServicio.obtenerMovimientosPorCaja(id);
            
            return ResponseEntity.ok(Map.of(
                "caja", caja,
                "movimientos", movimientos,
                "totalMovimientos", movimientos.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener resumen de caja", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Registrar movimiento manual
     */
    @PostMapping("/{id}/movimiento")
    public ResponseEntity<?> registrarMovimiento(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String tipo = request.get("tipo").toString();
            BigDecimal monto = new BigDecimal(request.get("monto").toString());
            String descripcion = request.get("descripcion").toString();
            String usuario = request.get("usuario").toString();
            String referencia = request.getOrDefault("referencia", "").toString();

            MovimientoCaja.TipoMovimiento tipoMovimiento = 
                "INGRESO".equals(tipo) ? MovimientoCaja.TipoMovimiento.INGRESO : MovimientoCaja.TipoMovimiento.EGRESO;

            MovimientoCaja movimiento = cajaServicio.registrarMovimiento(id, tipoMovimiento, monto, descripcion, usuario, referencia);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimiento registrado correctamente",
                "movimiento", movimiento
            ));
        } catch (Exception e) {
            logger.error("Error al registrar movimiento", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Registrar egreso manual (gasto)
     */
    @PostMapping("/{id}/egreso")
    public ResponseEntity<?> registrarEgreso(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal monto = new BigDecimal(request.get("monto").toString());
            String descripcion = request.get("descripcion").toString();
            String usuario = request.get("usuario").toString();
            String observaciones = request.getOrDefault("observaciones", "").toString();

            MovimientoCaja movimiento = cajaServicio.registrarEgresoManual(id, monto, descripcion, usuario, observaciones);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Egreso registrado correctamente",
                "movimiento", movimiento
            ));
        } catch (Exception e) {
            logger.error("Error al registrar egreso", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtener historial de cajas
     */
    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorial(@RequestParam(defaultValue = "30") int dias) {
        try {
            List<CajaDiaria> cajas = cajaServicio.obtenerHistorialCajas(dias);
            
            return ResponseEntity.ok(Map.of(
                "cajas", cajas,
                "total", cajas.size()
            ));
        } catch (Exception e) {
            logger.error("Error al obtener historial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Generar reporte PDF de caja
     */
    @GetMapping("/{id}/reporte")
    public ResponseEntity<?> generarReporte(@PathVariable Long id) {
        try {
            byte[] reporte = cajaServicio.generarReporteCaja(id);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=reporte_caja_" + id + ".pdf")
                .body(reporte);
        } catch (Exception e) {
            logger.error("Error al generar reporte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Verificar si existe caja abierta
     */
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarCajaAbierta() {
        try {
            boolean existeCaja = cajaServicio.existeCajaAbierta();
            
            return ResponseEntity.ok(Map.of(
                "existeCajaAbierta", existeCaja
            ));
        } catch (Exception e) {
            logger.error("Error al verificar caja abierta", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

