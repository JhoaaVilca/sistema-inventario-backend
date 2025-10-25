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
import tienda.inventario.servicios.PdfServicio;
import tienda.inventario.servicios.ICajaDiariaServicio;
import tienda.inventario.modelo.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

	@Autowired
	private PdfServicio pdfServicio;

	@Autowired
	private ICajaDiariaServicio cajaServicio;

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

			// Integración con Caja del Día - Registrar ingreso si es venta al contado
			try {
				if (creada.getTipoVenta() != null && "CONTADO".equals(creada.getTipoVenta())) {
					// Verificar si hay caja abierta
					if (cajaServicio.existeCajaAbierta()) {
						var cajaAbierta = cajaServicio.obtenerCajaAbierta();
						if (cajaAbierta.isPresent()) {
							cajaServicio.registrarIngresoVenta(
								cajaAbierta.get().getIdCaja(),
								BigDecimal.valueOf(creada.getTotalSalida()),
								"Venta al contado - " + creada.getIdSalida(),
								"admin", // TODO: Obtener usuario actual
								creada.getIdSalida()
							);
							logger.info("Ingreso registrado en caja por venta al contado: {}", creada.getTotalSalida());
						}
					} else {
						logger.warn("No hay caja abierta para registrar el ingreso de la venta");
					}
				}
			} catch (Exception e) {
				logger.error("Error al registrar ingreso en caja: ", e);
				// No fallar la venta si hay error con la caja
			}

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

	@GetMapping("/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		try {
			var salida = salidaServicio.obtenerPorId(id);
			return ResponseEntity.ok(SalidaMapper.toResponse(salida));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
		}
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
		}
	}

	@PutMapping("/{id}/cancelar")
	public ResponseEntity<?> cancelarSalida(@PathVariable Long id) {
		try {
			var cancelada = salidaServicio.cancelarSalida(id);
			return ResponseEntity.ok(SalidaMapper.toResponse(cancelada));
		} catch (Exception e) {
			logger.error("Error al cancelar salida: ", e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
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

	@GetMapping("/{id}/boleta")
	public ResponseEntity<byte[]> generarBoleta(@PathVariable Long id) {
		try {
			var salida = salidaServicio.obtenerPorId(id);
			byte[] pdfBytes = pdfServicio.generarBoletaVenta(salida);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.setContentDispositionFormData("attachment", "boleta_venta_" + id + ".pdf");
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error al generar boleta de venta: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}/ticket-pdf")
	public ResponseEntity<byte[]> generarTicketPdf(@PathVariable Long id) {
		try {
			var salida = salidaServicio.obtenerPorId(id);
			byte[] pdfBytes = pdfServicio.generarTicketSalida80mm(salida);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ticket_" + id + ".pdf");
			headers.setCacheControl("no-cache, no-store, must-revalidate");
			headers.setPragma("no-cache");
			headers.setExpires(0);

			return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error al generar ticket PDF: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Obtener estado de caja para el módulo de Salidas
	 */
	@GetMapping("/caja/estado")
	public ResponseEntity<?> obtenerEstadoCaja() {
		try {
			boolean existeCaja = cajaServicio.existeCajaAbierta();
			
			if (existeCaja) {
				var cajaAbierta = cajaServicio.obtenerCajaAbierta();
				if (cajaAbierta.isPresent()) {
					var caja = cajaAbierta.get();
					return ResponseEntity.ok(Map.of(
						"existeCaja", true,
						"caja", Map.of(
							"id", caja.getIdCaja(),
							"fecha", caja.getFecha(),
							"montoApertura", caja.getMontoApertura(),
							"totalIngresos", caja.getTotalIngresos(),
							"totalEgresos", caja.getTotalEgresos(),
							"saldoActual", caja.getSaldoActual(),
							"fechaApertura", caja.getFechaApertura(),
							"usuarioApertura", caja.getUsuarioApertura()
						)
					));
				}
			}
			
			return ResponseEntity.ok(Map.of(
				"existeCaja", false,
				"message", "No hay caja abierta"
			));
		} catch (Exception e) {
			logger.error("Error al obtener estado de caja: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("success", false, "message", e.getMessage()));
		}
	}

}
