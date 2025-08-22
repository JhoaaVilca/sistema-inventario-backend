package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tienda.inventario.dto.SalidaRequestDTO;
import tienda.inventario.dto.SalidaResponseDTO;
import tienda.inventario.mapper.SalidaMapper;
import tienda.inventario.modelo.Salida;
import tienda.inventario.repositorio.ProductoRepositorio;
import tienda.inventario.servicios.ISalidaServicio;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/salidas")
public class SalidaControlador {

	@Autowired
	private ISalidaServicio salidaServicio;

	@Autowired
	private ProductoRepositorio productoRepositorio;

	private static final Logger logger = LoggerFactory.getLogger(SalidaControlador.class);

	@PostMapping
	public ResponseEntity<?> guardar(@Valid @RequestBody SalidaRequestDTO request) {
		try {
			var ids = request.getDetalles().stream().map(d -> d.getIdProducto()).distinct().toList();
			var productos = productoRepositorio.findAllById(ids);
			Salida salida = SalidaMapper.toEntity(request, productos);
			Salida creada = salidaServicio.guardarSalida(salida);
			SalidaResponseDTO response = SalidaMapper.toResponse(creada);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Error al registrar salida: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error al registrar salida: " + e.getMessage());
		}
	}

	@GetMapping
	public List<SalidaResponseDTO> listar() {
		return salidaServicio.listarSalidas().stream().map(SalidaMapper::toResponse).toList();
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarSalida(@PathVariable Long id, @Valid @RequestBody SalidaRequestDTO request){
		try {
			var ids = request.getDetalles().stream().map(d -> d.getIdProducto()).distinct().toList();
			var productos = productoRepositorio.findAllById(ids);
			Salida salida = SalidaMapper.toEntity(request, productos);
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


