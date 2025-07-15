package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.modelo.Producto;
import tienda.inventario.servicios.IProductoServicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController

//http://localhost:8080/api/productos

@RequestMapping("/api/productos") // Ruta clara y RESTful
@CrossOrigin(origins = "http://localhost:3001") // Solo mientras desarrollas el frontend local
public class ProductoControlador {

    @Autowired
    private IProductoServicio servicio;

    // GET: Listar todos los productos
    @GetMapping
    public List<Producto> listarProductos() {
        return servicio.listarProductos();
    }

    // GET: Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        Optional<Producto> producto = servicio.obtenerProductoPorId(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Crear nuevo producto
    @PostMapping
    public Producto guardarProducto(@RequestBody Producto producto) {
        return servicio.guardarProducto(producto);
    }

    // PUT: Actualizar producto por ID
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoUpdate) {
        Optional<Producto> productoOptional = servicio.obtenerProductoPorId(id);

        if (productoOptional.isPresent()) {
            Producto producto = productoOptional.get();
            producto.setNombreProducto(productoUpdate.getNombreProducto());
            producto.setPrecio(productoUpdate.getPrecio());
            producto.setStock(productoUpdate.getStock());
            producto.setCategoria(productoUpdate.getCategoria());
            producto.setFechaIngreso(productoUpdate.getFechaIngreso());

            Producto actualizado = servicio.guardarProducto(producto);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE: Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarProducto(@PathVariable Long id) {
        servicio.eliminarProducto(id);
        Map<String, Boolean> respuesta = new HashMap<>();
        respuesta.put("eliminado", true);
        return ResponseEntity.ok(respuesta);
    }
}
