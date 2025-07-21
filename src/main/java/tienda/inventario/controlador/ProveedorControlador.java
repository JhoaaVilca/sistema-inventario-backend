package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Proveedor;
import tienda.inventario.servicios.ApiConsultaService;
import tienda.inventario.servicios.IProveedorServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "http://localhost:3001")
public class ProveedorControlador {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorControlador.class);

    @Autowired
    private IProveedorServicio proveedorServicio;

    @Autowired
    private ApiConsultaService apiConsultaService;

    @GetMapping
    public List<Proveedor> listarProveedores() {
        return proveedorServicio.listarProveedores();
    }

    @PostMapping
    public Proveedor guardarProveedor(@RequestBody Proveedor proveedor) {
        return proveedorServicio.guardarProveedor(proveedor);
    }

    @DeleteMapping("/{id}")
    public void eliminarProveedor(@PathVariable Long id) {
        proveedorServicio.eliminarProveedor(id);
    }

    @PutMapping("/{id}")
    public Proveedor actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        return proveedorServicio.actualizarProveedor(id, proveedor);
    }

    @GetMapping("/buscar")
    public Proveedor buscarPorDocumento(@RequestParam String numero) {
        return proveedorServicio.buscarPorDocumento(numero)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con documento: " + numero));
    }

    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarProveedor(
            @RequestParam String tipo,
            @RequestParam String numero) {
        
        logger.info("Iniciando consulta de documento - Tipo: {}, Número: {}", tipo, numero);
        
        try {
            // Validar parámetros
            if (tipo == null || tipo.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El tipo de documento es requerido"));
            }
            
            if (numero == null || numero.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número de documento es requerido"));
            }
            
            // Validar formato según tipo
            if (tipo.equalsIgnoreCase("DNI") && numero.length() != 8) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El DNI debe tener 8 dígitos"));
            }
            
            if (tipo.equalsIgnoreCase("RUC") && numero.length() != 11) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El RUC debe tener 11 dígitos"));
            }
            
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento(tipo, numero);
            
            // Verificar si se obtuvieron datos válidos
            if (resultado.get("nombre") == null || resultado.get("nombre").trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontraron datos para el " + tipo + ": " + numero));
            }
            
            logger.info("Consulta exitosa para {}: {}", tipo, numero);
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error al consultar documento {}: {}", tipo + " " + numero, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/consultar/ruc/{numero}")
    public ResponseEntity<Map<String, Object>> consultarRUC(@PathVariable String numero) {
        logger.info("Consultando RUC: {}", numero);
        
        try {
            if (numero == null || numero.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número de RUC es requerido"));
            }
            
            if (numero.length() != 11) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El RUC debe tener 11 dígitos"));
            }
            
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento("RUC", numero);
            
            if (resultado.get("nombre") == null || resultado.get("nombre").trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontraron datos para el RUC: " + numero));
            }
            
            logger.info("Consulta RUC exitosa: {}", numero);
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
            
        } catch (Exception e) {
            logger.error("Error al consultar RUC {}: {}", numero, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al consultar RUC: " + e.getMessage()));
        }
    }

    @GetMapping("/consultar/dni/{numero}")
    public ResponseEntity<Map<String, Object>> consultarDNI(@PathVariable String numero) {
        logger.info("Consultando DNI: {}", numero);
        
        try {
            if (numero == null || numero.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El número de DNI es requerido"));
            }
            
            if (numero.length() != 8) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El DNI debe tener 8 dígitos"));
            }
            
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento("DNI", numero);
            
            if (resultado.get("nombre") == null || resultado.get("nombre").trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontraron datos para el DNI: " + numero));
            }
            
            logger.info("Consulta DNI exitosa: {}", numero);
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
            
        } catch (Exception e) {
            logger.error("Error al consultar DNI {}: {}", numero, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al consultar DNI: " + e.getMessage()));
        }
    }

    @GetMapping("/test/raw/{tipo}/{numero}")
    public ResponseEntity<Map<String, Object>> testRawResponse(@PathVariable String tipo, @PathVariable String numero) {
        logger.info("Test raw response para {}: {}", tipo, numero);
        
        try {
            String url;
            if (tipo.equalsIgnoreCase("DNI")) {
                url = "https://dniruc.apisperu.com/api/v1/dni/" + numero + "?token=" + 
                      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Impob2FubmF2aWxjYUBnbWFpbC5jb20ifQ.mwmV-IM0AeieH3g1n4Ef2ee8PtUprxixwEGhWjVBYZw";
            } else if (tipo.equalsIgnoreCase("RUC")) {
                url = "https://dniruc.apisperu.com/api/v1/ruc/" + numero + "?token=" + 
                      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Impob2FubmF2aWxjYUBnbWFpbC5jb20ifQ.mwmV-IM0AeieH3g1n4Ef2ee8PtUprxixwEGhWjVBYZw";
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tipo inválido. Use 'RUC' o 'DNI'"));
            }
            
            RestTemplate restTemplate = new RestTemplate();
            String resultado = restTemplate.getForObject(url, String.class);
            
            logger.info("Respuesta raw de la API: {}", resultado);
            
            return ResponseEntity.ok(Map.of(
                "url", url,
                "raw_response", resultado,
                "tipo", tipo,
                "numero", numero
            ));
            
        } catch (Exception e) {
            logger.error("Error en test raw response: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error en test: " + e.getMessage()));
        }
    }
}
