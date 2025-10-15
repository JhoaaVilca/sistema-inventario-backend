package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.KardexRequestDTO;
import tienda.inventario.dto.KardexResponseDTO;
import tienda.inventario.mapper.KardexMapper;
import tienda.inventario.servicios.KardexServicio;
import tienda.inventario.servicios.PdfServicio;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin(origins = "http://localhost:3001")
public class KardexControlador {

    @Autowired
    private KardexServicio kardexServicio;

    @Autowired
    private PdfServicio pdfServicio;

    // Obtener movimientos de Kardex con filtros y paginación
    @GetMapping
    public ResponseEntity<Page<KardexResponseDTO>> obtenerMovimientosKardex(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build(); // O manejar el error de otra forma
        }

        Page<KardexResponseDTO> movimientos;
        if (idProducto != null && inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYFechas(idProducto, inicio, fin, pageable)
                    .map(KardexMapper::toResponse);
        } else if (idProducto != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProducto(idProducto, pageable)
                    .map(KardexMapper::toResponse);
        } else if (inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorFechas(inicio, fin, pageable)
                    .map(KardexMapper::toResponse);
        } else {
            movimientos = kardexServicio.obtenerTodosLosMovimientos(pageable)
                    .map(KardexMapper::toResponse);
        }
        return ResponseEntity.ok(movimientos);
    }

    // Endpoint para crear un ajuste manual en el Kardex
    @PostMapping("/ajuste")
    public ResponseEntity<KardexResponseDTO> crearAjusteManual(@Valid @RequestBody KardexRequestDTO request) {
        try {
            KardexResponseDTO nuevoAjuste = KardexMapper.toResponse(kardexServicio.crearAjusteManual(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAjuste);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Generar PDF del Kardex
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarKardexPdf(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) throws IOException {

        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        try {
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        List<KardexResponseDTO> movimientos;
        if (idProducto != null && inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYFechas(idProducto, inicio, fin, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (idProducto != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProducto(idProducto, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorFechas(inicio, fin, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else {
            movimientos = kardexServicio.obtenerTodosLosMovimientos(Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        }

        byte[] pdfBytes = pdfServicio.generarKardexPdf(movimientos, idProducto, inicio, fin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "kardex_reporte_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

