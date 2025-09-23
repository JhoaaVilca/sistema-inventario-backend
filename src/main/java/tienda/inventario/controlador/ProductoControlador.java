package tienda.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.ProductoRequestDTO;
import tienda.inventario.dto.ProductoResponseDTO;
import tienda.inventario.mapper.ProductoMapper;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.CategoriaRepositorio;
import tienda.inventario.servicios.IProductoServicio;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final IProductoServicio servicio;
    private final CategoriaRepositorio categoriaRepositorio;

    public ProductoControlador(IProductoServicio servicio, CategoriaRepositorio categoriaRepositorio) {
        this.servicio = servicio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    // ✅ GET: Listar todos los productos
    @GetMapping
    public List<ProductoResponseDTO> listarProductos() {
        return servicio.listarProductos()
                .stream()
                .map(ProductoMapper::toResponse)
                .toList();
    }

    // ✅ GET: Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Long id) {
        return servicio.obtenerProductoPorId(id)
                .map(ProductoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ POST: Crear nuevo producto
    @PostMapping
    public ResponseEntity<?> guardarProducto(@RequestBody ProductoRequestDTO dto) {
        try {
            Map<String, String> error = new HashMap<>();

            if (dto == null) {
                error.put("error", "Los datos del producto no pueden ser nulos");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getNombreProducto() == null || dto.getNombreProducto().trim().isEmpty()) {
                error.put("error", "El nombre del producto es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getPrecio() == null || dto.getPrecio() <= 0) {
                error.put("error", "El precio de venta debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getPrecioCompra() == null || dto.getPrecioCompra() <= 0) {
                error.put("error", "El precio de compra debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getStock() == null || dto.getStock() < 0) {
                error.put("error", "El stock no puede ser negativo");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getStockMinimo() == null || dto.getStockMinimo() < 0) {
                error.put("error", "El stock mínimo no puede ser negativo");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getUnidadMedida() == null || dto.getUnidadMedida().trim().isEmpty()) {
                error.put("error", "La unidad de medida es obligatoria");
                return ResponseEntity.badRequest().body(error);
            }


            if (dto.getFechaIngreso() == null) {
                error.put("error", "La fecha de ingreso es obligatoria");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getIdCategoria() == null || dto.getIdCategoria() <= 0) {
                error.put("error", "Debe seleccionar una categoría válida");
                return ResponseEntity.badRequest().body(error);
            }

            Categoria categoria = categoriaRepositorio.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

            Producto producto = ProductoMapper.toEntity(dto, categoria);

            Producto guardado = servicio.guardarProducto(producto);

            ProductoResponseDTO resp = ProductoMapper.toResponse(guardado);
            return ResponseEntity.created(URI.create("/api/productos/" + guardado.getIdProducto())).body(resp);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ PUT: Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequestDTO dto) {
        return servicio.obtenerProductoPorId(id).map(existente -> {
            try {
                Categoria categoria = categoriaRepositorio.findById(dto.getIdCategoria())
                        .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + dto.getIdCategoria()));

                // Actualizar todos los campos
                existente.setNombreProducto(dto.getNombreProducto());
                existente.setPrecio(dto.getPrecio()); // Precio de venta
                existente.setPrecioCompra(dto.getPrecioCompra()); // Precio de compra
                existente.setStock(dto.getStock());
                existente.setStockMinimo(dto.getStockMinimo());
                existente.setUnidadMedida(dto.getUnidadMedida());
                existente.setFechaIngreso(dto.getFechaIngreso());
                existente.setEsPerecible(dto.getEsPerecible());
                existente.setDescripcionCorta(dto.getDescripcionCorta());
                existente.setCategoria(categoria);

                Producto actualizado = servicio.guardarProducto(existente);
                return ResponseEntity.ok(ProductoMapper.toResponse(actualizado));

            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
        }).orElseGet(() -> {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Producto no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        });
    }

    // ✅ DELETE: Eliminar producto con mejor manejo de errores
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            if (!servicio.obtenerProductoPorId(id).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Producto no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }

            servicio.eliminarProducto(id);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Producto eliminado exitosamente");
            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor al eliminar el producto");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ GET: Obtener productos con stock bajo
    @GetMapping("/alertas/stock-bajo")
    public List<ProductoResponseDTO> obtenerProductosStockBajo() {
        return servicio.listarProductos()
                .stream()
                .map(ProductoMapper::toResponse)
                .filter(producto -> Boolean.TRUE.equals(producto.getStockBajo()))
                .toList();
    }


    // ✅ GET: Obtener resumen de alertas de stock
    @GetMapping("/alertas/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumenAlertas() {
        List<ProductoResponseDTO> todosProductos = servicio.listarProductos()
                .stream()
                .map(ProductoMapper::toResponse)
                .toList();

        long stockBajo = todosProductos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getStockBajo()))
                .count();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("stockBajo", stockBajo);
        resumen.put("totalProductos", todosProductos.size());

        return ResponseEntity.ok(resumen);
    }

    // ✅ GET: Buscar productos por nombre (para autocompletado)
    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscarProductos(@RequestParam String q) {
        return servicio.buscarProductosPorNombre(q)
                .stream()
                .map(ProductoMapper::toResponse)
                .toList();
    }
}
