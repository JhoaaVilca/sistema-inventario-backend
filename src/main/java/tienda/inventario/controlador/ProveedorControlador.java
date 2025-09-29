package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
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
@Validated
public class ProveedorControlador {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorControlador.class);

    @Autowired
    private IProveedorServicio proveedorServicio;

    @Autowired
    private ApiConsultaService apiConsultaService;

    // ✅ Listar TODOS (activos e inactivos)
    @GetMapping
    public Page<Proveedor> listarProveedores(@PageableDefault(size = 20, sort = "idProveedor", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return proveedorServicio.listarProveedores(pageable);
    }

    @PostMapping
    public Proveedor guardarProveedor(@Valid @RequestBody Proveedor proveedor) {
        return proveedorServicio.guardarProveedor(proveedor);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<String> desactivarProveedor(@PathVariable Long id) {
        proveedorServicio.desactivarProveedor(id);
        return ResponseEntity.ok("Proveedor desactivado correctamente");
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<String> activarProveedor(@PathVariable Long id) {
        proveedorServicio.activarProveedor(id);
        return ResponseEntity.ok("Proveedor activado correctamente");
    }

    @PutMapping("/{id}")
    public Proveedor actualizarProveedor(@PathVariable Long id, @Valid @RequestBody Proveedor proveedor) {
        return proveedorServicio.actualizarProveedor(id, proveedor);
    }

    @GetMapping("/buscar")
    public Proveedor buscarPorDocumento(@RequestParam String numero) {
        return proveedorServicio.buscarPorDocumento(numero)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con documento: " + numero));
    }
    @GetMapping("/activos")
    public Page<Proveedor> listarProveedoresActivos(@PageableDefault(size = 1000) Pageable pageable) {
        return proveedorServicio.listarProveedoresActivos(pageable);
    }


    // 🔽 Tus métodos de consulta (se mantienen igual)
    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarProveedor(
            @RequestParam String tipo,
            @RequestParam String numero) {
        try {
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento(tipo, numero);
            if (resultado.get("nombre") == null || resultado.get("nombre").trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontraron datos para el " + tipo + ": " + numero));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/consultar/ruc/{numero}")
    public ResponseEntity<Map<String, Object>> consultarRUC(@PathVariable String numero) {
        try {
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento("RUC", numero);
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al consultar RUC: " + e.getMessage()));
        }
    }

    @GetMapping("/consultar/dni/{numero}")
    public ResponseEntity<Map<String, Object>> consultarDNI(@PathVariable String numero) {
        try {
            Map<String, String> resultado = apiConsultaService.consultarPorDocumento("DNI", numero);
            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al consultar DNI: " + e.getMessage()));
        }
    }

    @GetMapping("/test/raw/{tipo}/{numero}")
    public ResponseEntity<Map<String, Object>> testRawResponse(@PathVariable String tipo, @PathVariable String numero) {
        try {
            String url;
            if (tipo.equalsIgnoreCase("DNI")) {
                url = "https://dniruc.apisperu.com/api/v1/dni/" + numero + "?token=TOKEN_AQUI";
            } else if (tipo.equalsIgnoreCase("RUC")) {
                url = "https://dniruc.apisperu.com/api/v1/ruc/" + numero + "?token=TOKEN_AQUI";
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tipo inválido. Use 'RUC' o 'DNI'"));
            }

            RestTemplate restTemplate = new RestTemplate();
            String resultado = restTemplate.getForObject(url, String.class);

            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "raw_response", resultado,
                    "tipo", tipo,
                    "numero", numero
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en test: " + e.getMessage()));
        }
    }
}
