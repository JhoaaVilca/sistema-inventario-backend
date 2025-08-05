package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.servicios.IEntradaServicio;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/entradas")
@CrossOrigin(origins = "*")
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
}
