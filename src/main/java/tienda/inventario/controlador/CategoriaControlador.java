package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.servicios.ICategoriaServicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:3001") // Solo en desarrollo
public class CategoriaControlador {

    @Autowired
    private ICategoriaServicio servicio;

    // GET: Listar todas las categorías
    @GetMapping
    public List<Categoria> listarCategorias() {
        return servicio.listarCategorias();
    }

    // GET: Buscar categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerCategoriaPorId(@PathVariable Long id) {
        Optional<Categoria> categoria = servicio.obtenerCategoriaPorId(id);
        return categoria.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Crear nueva categoría
    @PostMapping
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        return servicio.guardarCategoria(categoria);
    }

    // PUT: Actualizar categoría
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoriaUpdate) {
        Optional<Categoria> categoriaOptional = servicio.obtenerCategoriaPorId(id);

        if (categoriaOptional.isPresent()) {
            Categoria categoria = categoriaOptional.get();
            categoria.setNombre(categoriaUpdate.getNombre());

            Categoria actualizado = servicio.guardarCategoria(categoria);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar categoría con mejor manejo de errores
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            // Verificar si la categoría existe
            if (!servicio.obtenerCategoriaPorId(id).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Categoría no encontrada con ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            // Intentar eliminar la categoría
            servicio.eliminarCategoria(id);
            
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Categoría eliminada exitosamente");
            return ResponseEntity.ok(respuesta);
            
        } catch (IllegalArgumentException e) {
            // Categoría no encontrada
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (RuntimeException e) {
            // Error al eliminar (restricciones de BD, etc.)
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            // Error inesperado
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor al eliminar la categoría");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
