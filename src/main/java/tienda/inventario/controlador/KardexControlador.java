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
import tienda.inventario.dto.KardexResumenDTO;
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
            @RequestParam(required = false) String tipoMovimiento,
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

        // Normalizar tipoMovimiento
        if (tipoMovimiento != null) {
            tipoMovimiento = tipoMovimiento.trim().toUpperCase();
            if (!tipoMovimiento.isEmpty() && !java.util.Set.of("ENTRADA", "SALIDA", "AJUSTE").contains(tipoMovimiento)) {
                return ResponseEntity.badRequest().build();
            }
        }

        Page<KardexResponseDTO> movimientos;
        if (idProducto != null && inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYFechasYTipo(idProducto, inicio, fin, tipoMovimiento, pageable)
                    .map(KardexMapper::toResponse);
        } else if (idProducto != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYTipo(idProducto, tipoMovimiento, pageable)
                    .map(KardexMapper::toResponse);
        } else if (inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorFechasYTipo(inicio, fin, tipoMovimiento, pageable)
                    .map(KardexMapper::toResponse);
        } else {
            movimientos = kardexServicio.obtenerTodosLosMovimientosPorTipo(tipoMovimiento, pageable)
                    .map(KardexMapper::toResponse);
        }
        return ResponseEntity.ok(movimientos);
    }

    // Exportar a CSV (compatible con Excel) movimientos + resumen
    @GetMapping(value = "/exportar-excel", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportarKardexExcel(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipoMovimiento) {

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

        // Normalizar tipoMovimiento
        if (tipoMovimiento != null) {
            tipoMovimiento = tipoMovimiento.trim().toUpperCase();
            if (!tipoMovimiento.isEmpty() && !java.util.Set.of("ENTRADA", "SALIDA", "AJUSTE").contains(tipoMovimiento)) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Normalizar tipoMovimiento
        if (tipoMovimiento != null) {
            tipoMovimiento = tipoMovimiento.trim().toUpperCase();
            if (!tipoMovimiento.isEmpty() && !java.util.Set.of("ENTRADA", "SALIDA", "AJUSTE").contains(tipoMovimiento)) {
                return ResponseEntity.badRequest().build();
            }
        }

        List<KardexResponseDTO> movimientos;
        if (idProducto != null && inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYFechasYTipo(idProducto, inicio, fin, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (idProducto != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYTipo(idProducto, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorFechasYTipo(inicio, fin, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else {
            movimientos = kardexServicio.obtenerTodosLosMovimientosPorTipo(tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        }

        StringBuilder sb = new StringBuilder();
        // Usar punto y coma (;) como separador para mejor compatibilidad con Excel
        String sep = ";";
        
        sb.append("Reporte Kardex de Inventario\n");
        sb.append("Periodo").append(sep);
        sb.append((inicio != null && fin != null) ? ("Del " + fechaInicio + " al " + fechaFin) : "Todos los movimientos").append("\n\n");

        // Encabezados de Movimientos - nombres completos y descriptivos
        sb.append("Fecha y Hora").append(sep)
          .append("Tipo de Movimiento").append(sep)
          .append("Referencia del Documento").append(sep)
          .append("Cantidad").append(sep)
          .append("Precio Unitario").append(sep)
          .append("Valor Total").append(sep)
          .append("Stock Anterior").append(sep)
          .append("Stock Actual").append(sep)
          .append("Costo Promedio").append(sep)
          .append("Observaciones").append(sep)
          .append("Nombre del Producto").append(sep)
          .append("Código del Producto").append(sep)
          .append("Usuario que Registró").append("\n");
          
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (KardexResponseDTO m : movimientos) {
            sb.append(m.getFechaMovimiento() != null ? m.getFechaMovimiento().format(fmt) : "").append(sep)
              .append(safeExcel(m.getTipoMovimiento())).append(sep)
              .append(safeExcel(m.getReferenciaDocumento())).append(sep)
              .append(m.getCantidad() != null ? m.getCantidad() : 0).append(sep)
              .append(m.getPrecioUnitario() != null ? m.getPrecioUnitario() : java.math.BigDecimal.ZERO).append(sep)
              .append(m.getValorTotal() != null ? m.getValorTotal() : java.math.BigDecimal.ZERO).append(sep)
              .append(m.getStockAnterior() != null ? m.getStockAnterior() : 0).append(sep)
              .append(m.getStockActual() != null ? m.getStockActual() : 0).append(sep)
              .append(m.getCostoPromedioActual() != null ? m.getCostoPromedioActual() : java.math.BigDecimal.ZERO).append(sep)
              .append(safeExcel(m.getObservaciones())).append(sep)
              .append(safeExcel(m.getNombreProducto())).append(sep)
              .append(m.getIdProducto() != null ? m.getIdProducto() : "").append(sep)
              .append(safeExcel(m.getUsuarioRegistro()))
              .append('\n');
        }

        // Resumen
        sb.append("\nResumen Kardex\n");
        sb.append("Concepto").append(sep).append("Cantidad").append(sep).append("Valor").append("\n");
        tienda.inventario.dto.KardexResumenDTO resumen = kardexServicio.calcularResumen(idProducto, inicio, fin, tipoMovimiento);
        int entradasCant = resumen.getTotalEntradasCantidad() != null ? resumen.getTotalEntradasCantidad() : 0;
        int salidasCant = resumen.getTotalSalidasCantidad() != null ? resumen.getTotalSalidasCantidad() : 0;
        int saldoInicial = resumen.getSaldoInicial() != null ? resumen.getSaldoInicial() : 0;
        int stockFinal = resumen.getStockFinal() != null ? resumen.getStockFinal() : 0;
        java.math.BigDecimal entradasVal = resumen.getTotalEntradasValor() != null ? resumen.getTotalEntradasValor() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal salidasVal = resumen.getTotalSalidasValor() != null ? resumen.getTotalSalidasValor() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal costoPromFinal = resumen.getCostoPromedioFinal() != null ? resumen.getCostoPromedioFinal() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal costoTotalFinal = resumen.getCostoTotalFinal() != null ? resumen.getCostoTotalFinal() : java.math.BigDecimal.ZERO;

        sb.append("Saldo inicial").append(sep).append(saldoInicial).append(sep).append(0).append('\n');
        sb.append("Entradas totales").append(sep).append(entradasCant).append(sep).append(entradasVal).append('\n');
        sb.append("Salidas totales").append(sep).append(salidasCant).append(sep).append(salidasVal).append('\n');
        sb.append("Stock final").append(sep).append(stockFinal).append(sep).append(0).append('\n');
        sb.append("Costo promedio final").append(sep).append("").append(sep).append(costoPromFinal).append('\n');
        sb.append("Costo total final").append(sep).append("").append(sep).append(costoTotalFinal).append('\n');

        String bom = "\uFEFF"; // BOM para Excel en Windows (UTF-8)
        byte[] csv = (bom + sb.toString()).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "kardex_" + java.time.LocalDate.now() + ".csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    private String safe(String s) {
        if (s == null) return "";
        String v = s.replace("\n", " ").replace("\r", " ");
        if (v.contains(",") || v.contains("\"")) {
            v = '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    private String safeExcel(String s) {
        if (s == null) return "";
        String v = s.replace("\n", " ").replace("\r", " ").replace(";", ",");
        if (v.contains("\"")) {
            v = '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    // Endpoint para crear un ajuste manual en el Kardex
    @PostMapping("/ajuste")
    public ResponseEntity<?> crearAjusteManual(@Valid @RequestBody KardexRequestDTO request) {
        try {
            // Validar que el tipo de movimiento sea ENTRADA o SALIDA (para ajustes manuales)
            if (request.getTipoMovimiento() != null) {
                String tipoNormalizado = request.getTipoMovimiento().trim().toUpperCase();
                if (!"ENTRADA".equals(tipoNormalizado) && !"SALIDA".equals(tipoNormalizado)) {
                    return ResponseEntity.badRequest()
                            .body(java.util.Map.of("error", "El tipo de movimiento debe ser ENTRADA o SALIDA para ajustes manuales."));
                }
            }
            
            KardexResponseDTO nuevoAjuste = KardexMapper.toResponse(kardexServicio.crearAjusteManual(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAjuste);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al crear el ajuste: " + e.getMessage()));
        }
    }

    // Generar PDF del Kardex
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarKardexPdf(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipoMovimiento) throws IOException {

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
            movimientos = kardexServicio.obtenerMovimientosPorProductoYFechasYTipo(idProducto, inicio, fin, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (idProducto != null) {
            movimientos = kardexServicio.obtenerMovimientosPorProductoYTipo(idProducto, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else if (inicio != null && fin != null) {
            movimientos = kardexServicio.obtenerMovimientosPorFechasYTipo(inicio, fin, tipoMovimiento, Pageable.unpaged())
                    .stream().map(KardexMapper::toResponse).collect(Collectors.toList());
        } else {
            movimientos = kardexServicio.obtenerTodosLosMovimientosPorTipo(tipoMovimiento, Pageable.unpaged())
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

    // Resumen de Kardex (saldo inicial, totales y stock final)
    @GetMapping("/resumen")
    public ResponseEntity<KardexResumenDTO> obtenerResumen(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipoMovimiento) {

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

        // Normalizar tipoMovimiento
        if (tipoMovimiento != null) {
            tipoMovimiento = tipoMovimiento.trim().toUpperCase();
            if (!tipoMovimiento.isEmpty() && !java.util.Set.of("ENTRADA", "SALIDA", "AJUSTE").contains(tipoMovimiento)) {
                return ResponseEntity.badRequest().build();
            }
        }

        KardexResumenDTO resumen = kardexServicio.calcularResumen(idProducto, inicio, fin, tipoMovimiento);
        return ResponseEntity.ok(resumen);
    }
}



