package tienda.inventario.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.ClienteRequestDTO;
import tienda.inventario.dto.ClienteResponseDTO;
import tienda.inventario.dto.ClienteBusquedaDTO;
import tienda.inventario.modelo.Cliente;
import tienda.inventario.servicios.IClienteServicio;
import tienda.inventario.servicios.ClienteBusquedaService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:3001")
public class ClienteControlador {

    private final IClienteServicio servicio;
    private final ClienteBusquedaService busquedaService;

    public ClienteControlador(IClienteServicio servicio, ClienteBusquedaService busquedaService) {
        this.servicio = servicio;
        this.busquedaService = busquedaService;
    }

    // GET: Listar todos los clientes
    @GetMapping
    public List<ClienteResponseDTO> listarClientes() {
        return servicio.listarClientes()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // GET: Listar solo clientes activos
    @GetMapping("/activos")
    public List<ClienteResponseDTO> listarClientesActivos() {
        return servicio.listarClientesActivos()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // GET: Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable Long id) {
        return servicio.obtenerClientePorId(id)
                .map(this::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Obtener cliente por DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorDni(@PathVariable String dni) {
        return servicio.obtenerClientePorDni(dni)
                .map(this::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Buscar clientes por término
    @GetMapping("/buscar")
    public List<ClienteResponseDTO> buscarClientes(@RequestParam String termino) {
        return servicio.buscarClientes(termino)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // GET: Buscar cliente por DNI (combinando BD local y API RENIEC)
    @GetMapping("/buscar-dni/{dni}")
    public ResponseEntity<?> buscarClientePorDni(@PathVariable String dni) {
        try {
            ClienteBusquedaDTO resultado = busquedaService.buscarClientePorDni(dni);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en la búsqueda: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // POST: Crear nuevo cliente
    @PostMapping
    public ResponseEntity<?> guardarCliente(@RequestBody ClienteRequestDTO dto) {
        try {
            Map<String, String> error = new HashMap<>();

            if (dto == null) {
                error.put("error", "Los datos del cliente no pueden ser nulos");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getDni() == null || dto.getDni().trim().isEmpty()) {
                error.put("error", "El DNI es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getNombres() == null || dto.getNombres().trim().isEmpty()) {
                error.put("error", "Los nombres son obligatorios");
                return ResponseEntity.badRequest().body(error);
            }

            Cliente cliente = toEntity(dto);
            Cliente guardado = servicio.guardarCliente(cliente);

            ClienteResponseDTO resp = toResponseDTO(guardado);
            return ResponseEntity.created(URI.create("/api/clientes/" + guardado.getIdCliente())).body(resp);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // PUT: Actualizar cliente existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Long id, @RequestBody ClienteRequestDTO dto) {
        try {
            Map<String, String> error = new HashMap<>();

            if (dto == null) {
                error.put("error", "Los datos del cliente no pueden ser nulos");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getDni() == null || dto.getDni().trim().isEmpty()) {
                error.put("error", "El DNI es obligatorio");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getNombres() == null || dto.getNombres().trim().isEmpty()) {
                error.put("error", "Los nombres son obligatorios");
                return ResponseEntity.badRequest().body(error);
            }

            Cliente cliente = toEntity(dto);
            Cliente actualizado = servicio.actualizarCliente(id, cliente);

            ClienteResponseDTO resp = toResponseDTO(actualizado);
            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // DELETE: Eliminar cliente (lógica)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(@PathVariable Long id) {
        try {
            servicio.eliminarCliente(id);
            Map<String, String> mensaje = new HashMap<>();
            mensaje.put("mensaje", "Cliente eliminado exitosamente");
            return ResponseEntity.ok(mensaje);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Métodos auxiliares para conversión
    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getIdCliente(),
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getFechaRegistro(),
                cliente.getActivo()
        );
    }

    private Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setDni(dto.getDni());
        cliente.setNombres(dto.getNombres());
        cliente.setApellidos(dto.getApellidos());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        return cliente;
    }
}
