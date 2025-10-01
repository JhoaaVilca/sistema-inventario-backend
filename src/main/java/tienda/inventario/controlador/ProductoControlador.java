package tienda.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.ProductoRequestDTO;
import tienda.inventario.dto.ProductoResponseDTO;
import tienda.inventario.mapper.ProductoMapper;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.modelo.Producto;
import tienda.inventario.modelo.DetalleEntrada;
import tienda.inventario.modelo.Lote;
import tienda.inventario.repositorio.CategoriaRepositorio;
import tienda.inventario.servicios.IProductoServicio;
import tienda.inventario.repositorio.DetalleEntradaRepositorio;
import tienda.inventario.repositorio.LoteRepositorio;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/productos")
@Validated
public class ProductoControlador {

    private final IProductoServicio servicio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final DetalleEntradaRepositorio detalleEntradaRepositorio;
    private final LoteRepositorio loteRepositorio;

    private static final Logger logger = LoggerFactory.getLogger(ProductoControlador.class);

    public ProductoControlador(IProductoServicio servicio, CategoriaRepositorio categoriaRepositorio,
                               DetalleEntradaRepositorio detalleEntradaRepositorio,
                               LoteRepositorio loteRepositorio) {
        this.servicio = servicio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.detalleEntradaRepositorio = detalleEntradaRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    // ✅ GET: Listar productos paginados
    @GetMapping
    public Page<ProductoResponseDTO> listarProductos(@PageableDefault(size = 20, sort = "idProducto", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return servicio.listarProductos(pageable)
                .map(ProductoMapper::toResponse);
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
    public ResponseEntity<?> guardarProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        try {
            Map<String, String> error = new HashMap<>();
            if (dto.getIdCategoria() == null || dto.getIdCategoria() <= 0) {
                error.put("error", "Debe seleccionar una categoría válida");
                return ResponseEntity.badRequest().body(error);
            }

            Categoria categoria = categoriaRepositorio.findById(dto.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + dto.getIdCategoria()));

            Producto producto = ProductoMapper.toEntity(dto, categoria);
            // Log diagnóstico de campos críticos
            logger.info("Creando producto: perecible={}, stock={}, fechaVencimientoInicial={}",
                    dto.getEsPerecible(), dto.getStock(), dto.getFechaVencimientoInicial());
            // Si llega una fecha de vencimiento inicial, marcamos como perecible
            if (dto.getFechaVencimientoInicial() != null) {
                producto.setEsPerecible(true);
            }

            Producto guardado = servicio.guardarProducto(producto);

            // Crear lote inicial si el producto tiene stock inicial > 0
            try {
                if (dto.getStock() != null && dto.getStock() > 0) {
                    // Determinar perecible efectivo (si llegó fecha, es perecible)
                    boolean esPerecibleEfectivo = Boolean.TRUE.equals(dto.getEsPerecible()) || dto.getFechaVencimientoInicial() != null;
                    // Validación: si es perecible efectivo, se requiere fecha de vencimiento inicial
                    if (esPerecibleEfectivo && dto.getFechaVencimientoInicial() == null) {
                        error.put("error", "Debe ingresar fecha de vencimiento para el lote inicial de un producto perecible");
                        return ResponseEntity.badRequest().body(error);
                    }
                    DetalleEntrada det = new DetalleEntrada();
                    det.setProducto(guardado);
                    det.setCantidad(dto.getStock());
                    det.setPrecioUnitario(dto.getPrecioCompra());
                    det.setSubtotal(dto.getPrecioCompra() != null ? dto.getPrecioCompra() * dto.getStock() : null);
                    det.setFechaVencimiento(dto.getFechaVencimientoInicial());

                    // Guardar primero el detalle para obtener su ID
                    DetalleEntrada detGuardado = detalleEntradaRepositorio.save(det);

                    // Crear y guardar el lote con la relación al detalle guardado
                    Lote lote = new Lote();
                    lote.setNumeroLote("INICIAL-" + LocalDate.now());
                    lote.setDetalleEntrada(detGuardado);
                    lote.setFechaEntrada(LocalDate.now());
                    lote.setFechaVencimiento(dto.getFechaVencimientoInicial());
                    lote.setEstado("Activo");
                    // Asegurar cantidadDisponible explícita para no depender solo de @PrePersist
                    lote.setCantidadDisponible(detGuardado.getCantidad());
                    detGuardado.setLote(lote);
                    loteRepositorio.save(lote);
                }
            } catch (Exception e) {
                logger.warn("No se pudo crear lote inicial para el producto: {}", e.getMessage());
            }

            ProductoResponseDTO resp = ProductoMapper.toResponse(guardado);
            return ResponseEntity.created(URI.create("/api/productos/" + guardado.getIdProducto())).body(resp);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error al crear producto", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ✅ PUT: Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
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
        return servicio.listarProductos(org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(ProductoMapper::toResponse)
                .filter(producto -> Boolean.TRUE.equals(producto.getStockBajo()))
                .toList();
    }


    // ✅ GET: Obtener resumen de alertas de stock
    @GetMapping("/alertas/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumenAlertas() {
        List<ProductoResponseDTO> todosProductos = servicio.listarProductos(org.springframework.data.domain.Pageable.unpaged())
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
