package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.servicios.IEntradaServicio;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/entradas")
public class EntradaControlador {

    @Autowired
    private IEntradaServicio entradaServicio;

    private static final Logger logger = LoggerFactory.getLogger(EntradaControlador.class);

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Entrada entrada) {
        try {
            Entrada entradaGuardada = entradaServicio.guardarEntrada(entrada);
            return ResponseEntity.ok(entradaGuardada);
        } catch (Exception e) {
            logger.error("Error al registrar entrada: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar entrada: " + e.getMessage());
        }
    }

    @GetMapping
    public Page<Entrada> listar(@PageableDefault(size = 20, sort = "idEntrada", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return entradaServicio.listarEntradas(pageable);
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
            @RequestParam(required = false) String numeroFactura,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        // Limpiar número de factura si viene vacío
        if (numeroFactura != null && numeroFactura.trim().isEmpty()) {
            numeroFactura = null;
        } else if (numeroFactura != null) {
            numeroFactura = numeroFactura.trim();
        }
        
        // Lógica de filtros combinados
        boolean tieneProveedor = idProveedor != null;
        boolean tieneFactura = numeroFactura != null && !numeroFactura.isEmpty();
        boolean tieneFechas = fechaInicio != null && fechaFin != null;
        
        // Caso 1: Proveedor + Factura + Fechas
        if (tieneProveedor && tieneFactura && tieneFechas) {
            return entradaServicio.filtrarPorProveedorYNumeroFacturaYRangoFechas(idProveedor, numeroFactura, fechaInicio, fechaFin);
        }
        // Caso 2: Proveedor + Factura
        else if (tieneProveedor && tieneFactura) {
            return entradaServicio.filtrarPorProveedorYNumeroFactura(idProveedor, numeroFactura);
        }
        // Caso 3: Proveedor + Fechas
        else if (tieneProveedor && tieneFechas) {
            return entradaServicio.filtrarPorProveedorYRangoFechas(idProveedor, fechaInicio, fechaFin);
        }
        // Caso 4: Factura + Fechas
        else if (tieneFactura && tieneFechas) {
            return entradaServicio.filtrarPorNumeroFacturaYRangoFechas(numeroFactura, fechaInicio, fechaFin);
        }
        // Caso 5: Solo Proveedor
        else if (tieneProveedor) {
            return entradaServicio.filtrarPorProveedor(idProveedor);
        }
        // Caso 6: Solo Factura
        else if (tieneFactura) {
            return entradaServicio.filtrarPorNumeroFactura(numeroFactura);
        }
        // Caso 7: Solo Fechas
        else if (tieneFechas) {
            return entradaServicio.filtrarPorRangoFechas(fechaInicio, fechaFin);
        }
        // Caso 8: Sin filtros
        else {
            return entradaServicio.listarEntradas();
        }
    }

    // ✅ POST: Subir factura de entrada
    @PostMapping("/{id}/factura")
    public ResponseEntity<?> subirFactura(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No se ha seleccionado ningún archivo");
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && 
                !contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest().body("Solo se permiten archivos PDF o imágenes");
            }

            String facturaUrl = entradaServicio.subirFactura(id, file);
            return ResponseEntity.ok().body("{\"facturaUrl\": \"" + facturaUrl + "\"}");

        } catch (Exception e) {
            logger.error("Error al subir factura: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al subir la factura: " + e.getMessage());
        }
    }

    // ✅ GET: Descargar factura de entrada
    @GetMapping("/{id}/factura")
    public ResponseEntity<Resource> descargarFactura(@PathVariable Long id) {
        try {
            Resource resource = entradaServicio.descargarFactura(id);
            
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            String filename = resource.getFilename();
            if (filename != null) {
                String lowerFilename = filename.toLowerCase();
                if (lowerFilename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerFilename.endsWith(".png")) {
                    contentType = "image/png";
                }
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (Exception e) {
            logger.error("Error al descargar factura: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
