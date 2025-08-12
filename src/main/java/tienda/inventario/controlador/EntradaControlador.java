package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.servicios.IEntradaServicio;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/entradas")
public class EntradaControlador {

    @Autowired
    private IEntradaServicio entradaServicio;

    private static final Logger logger = LoggerFactory.getLogger(EntradaControlador.class);

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Entrada entrada) {
        try {
            entradaServicio.guardarEntrada(entrada);
            return ResponseEntity.ok("Entrada registrada correctamente");
        } catch (Exception e) {
            logger.error("Error al registrar entrada: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar entrada: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Entrada> listar() {
        return entradaServicio.listarEntradas();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEntrada(@PathVariable Long id, @RequestBody Entrada entrada){
        try {
            Entrada entradaActualizada = entradaServicio.actualizarEntrada(id, entrada);
            return ResponseEntity.ok(entradaActualizada);
        } catch (Exception e) {
            logger.error("Error al actualizar entrada: ", e);
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<?> eliminarEntrada(@PathVariable Long id){
        try {
            entradaServicio.eliminarEntrada(id);
            return  ResponseEntity.ok("Entrada eliminada Correctamente");
        } catch (Exception e) {
            logger.error("Error al eliminar entrada: ", e);
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/filtrar/proveedor/{idProveedor}")
    public List<Entrada> filtrarPorProveedor(@PathVariable Long idProveedor) {
        return entradaServicio.filtrarPorProveedor(idProveedor);
    }

    @GetMapping("/filtrar/fecha")
    public List<Entrada> filtrarPorFecha(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return entradaServicio.filtrarPorFecha(fecha);
    }

    @GetMapping("/filtrar/rango")
    public List<Entrada> filtrarPorRango(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return entradaServicio.filtrarPorRangoFechas(inicio, fin);
    }

    @GetMapping("/filtrar")
    public List<Entrada> filtrarEntradas(
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        if (idProveedor != null && fechaInicio != null && fechaFin != null) {
            // Filtrar por proveedor y rango de fechas
            return entradaServicio.filtrarPorProveedorYRangoFechas(idProveedor, fechaInicio, fechaFin);
        } else if (idProveedor != null) {
            // Solo filtrar por proveedor
            return entradaServicio.filtrarPorProveedor(idProveedor);
        } else if (fechaInicio != null && fechaFin != null) {
            // Solo filtrar por rango de fechas
            return entradaServicio.filtrarPorRangoFechas(fechaInicio, fechaFin);
        } else {
            // Sin filtros, retornar todas
            return entradaServicio.listarEntradas();
        }
    }

}
