package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.CategoriaRequestDTO;
import tienda.inventario.dto.CategoriaResponseDTO;
import tienda.inventario.mapper.CategoriaMapper;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.servicios.ICategoriaServicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:3001") // Solo en desarrollo
public class CategoriaControlador {

    @Autowired
    private ICategoriaServicio servicio;

    // GET: Listar todas las categorías
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        try {
            List<CategoriaResponseDTO> categorias = servicio.listarCategorias()
                    .stream()
                    .map(CategoriaMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // GET: Listar solo categorías activas
    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategoriasActivas() {
        try {
            List<CategoriaResponseDTO> categorias = servicio.listarCategorias()
                    .stream()
                    .filter(Categoria::isActivo)
                    .map(CategoriaMapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            System.err.println("Error al listar categorías activas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // GET: Buscar categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerCategoriaPorId(@PathVariable Long id) {
        return servicio.obtenerCategoriaPorId(id)
                .map(c -> ResponseEntity.ok(CategoriaMapper.toResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Crear nueva categoría
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> guardarCategoria(@RequestBody CategoriaRequestDTO dto) {
        try {
            Categoria categoria = CategoriaMapper.toEntity(dto);
            Categoria guardada = servicio.guardarCategoria(categoria);
            return ResponseEntity.ok(CategoriaMapper.toResponse(guardada));
        } catch (Exception e) {
            System.err.println("Error al guardar categoría: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // PUT: Actualizar categoría
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody CategoriaRequestDTO dto) {

        Optional<Categoria> categoriaOptional = servicio.obtenerCategoriaPorId(id);
        if (categoriaOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Categoria categoria = categoriaOptional.get();
            categoria.setNombre(dto.getNombre());
            categoria.setDescripcion(dto.getDescripcion());
            if (dto.getActivo() != null) {
                categoria.setActivo(dto.getActivo());
            }

            Categoria actualizado = servicio.guardarCategoria(categoria);
            return ResponseEntity.ok(CategoriaMapper.toResponse(actualizado));
        } catch (Exception e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
