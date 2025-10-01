package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.validation.Valid;
import tienda.inventario.dto.SalidaRequestDTO;
import tienda.inventario.dto.SalidaResponseDTO;
import tienda.inventario.mapper.SalidaMapper;
import tienda.inventario.modelo.Salida;
import tienda.inventario.repositorio.ProductoRepositorio;
import tienda.inventario.repositorio.ClienteRepositorio;
import tienda.inventario.servicios.ISalidaServicio;
import tienda.inventario.modelo.Cliente;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/salidas")
@CrossOrigin(origins = "http://localhost:3001")
public class SalidaControlador {

	@Autowired
	private ISalidaServicio salidaServicio;

	@Autowired
	private ProductoRepositorio productoRepositorio;

	@Autowired
	private ClienteRepositorio clienteRepositorio;

	private static final Logger logger = LoggerFactory.getLogger(SalidaControlador.class);

	@PostMapping
	public ResponseEntity<?> guardar(@Valid @RequestBody SalidaRequestDTO request) {
		try {
			logger.info("Recibiendo solicitud de salida: {}", request);
			
			var ids = request.getDetalles().stream().map(d -> d.getProducto().getIdProducto()).distinct().toList();
			var productos = productoRepositorio.findAllById(ids);
			
			// Buscar el cliente
			Cliente cliente = null;
			if (request.getCliente() != null && request.getCliente().getIdCliente() != null) {
				logger.info("Buscando cliente con ID: {}", request.getCliente().getIdCliente());
				cliente = clienteRepositorio.findById(request.getCliente().getIdCliente())
					.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
				logger.info("Cliente encontrado: {} - {}", cliente.getDni(), cliente.getNombres());
			} else {
				logger.warn("No se proporcionó ID de cliente. Request cliente: {}", request.getCliente());
			}
			
			logger.info("Productos encontrados: {}", productos.size());
			logger.info("Detalles de la salida: {}", request.getDetalles().size());
			
			Salida salida = SalidaMapper.toEntity(request, productos, cliente);
			logger.info("Salida creada con cliente: {}", salida.getCliente() != null ? salida.getCliente().getDni() : "null");
			
			Salida creada = salidaServicio.guardarSalida(salida);
			logger.info("Salida guardada con ID: {} y cliente: {}", creada.getIdSalida(), 
				creada.getCliente() != null ? creada.getCliente().getDni() : "null");
			
			SalidaResponseDTO response = SalidaMapper.toResponse(creada);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Error al registrar salida: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error al registrar salida: " + e.getMessage());
		}
	}

    @GetMapping
    public Page<SalidaResponseDTO> listar(@PageableDefault(size = 20, sort = "idSalida", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return salidaServicio.listarSalidas(pageable).map(SalidaMapper::toResponse);
    }

	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarSalida(@PathVariable Long id, @Valid @RequestBody SalidaRequestDTO request){
		try {
			var ids = request.getDetalles().stream().map(d -> d.getProducto().getIdProducto()).distinct().toList();
			var productos = productoRepositorio.findAllById(ids);
			
			// Buscar el cliente
			Cliente cliente = null;
			if (request.getCliente() != null && request.getCliente().getIdCliente() != null) {
				cliente = clienteRepositorio.findById(request.getCliente().getIdCliente())
					.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
			}
			
			Salida salida = SalidaMapper.toEntity(request, productos, cliente);
			Salida salidaActualizada = salidaServicio.actualizarSalida(id, salida);
			return ResponseEntity.ok(SalidaMapper.toResponse(salidaActualizada));
		} catch (Exception e) {
			logger.error("Error al actualizar salida: ", e);
			return  ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Error: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public  ResponseEntity<?> eliminarSalida(@PathVariable Long id){
		try {
			salidaServicio.eliminarSalida(id);
			return  ResponseEntity.ok("Salida eliminada Correctamente");
		} catch (Exception e) {
			logger.error("Error al eliminar salida: ", e);
			return  ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Error: " + e.getMessage());
		}
	}

	@GetMapping("/filtrar/fecha")
	public List<SalidaResponseDTO> filtrarPorFecha(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return salidaServicio.filtrarPorFecha(fecha).stream().map(SalidaMapper::toResponse).toList();
	}

	@GetMapping("/filtrar/rango")
	public List<SalidaResponseDTO> filtrarPorRango(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
													 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
		return salidaServicio.filtrarPorRangoFechas(inicio, fin).stream().map(SalidaMapper::toResponse).toList();
	}
	

}


