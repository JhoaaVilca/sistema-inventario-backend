package tienda.inventario.servicios;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tienda.inventario.dto.KardexResponseDTO;
import tienda.inventario.modelo.Producto;
import tienda.inventario.modelo.Salida;
import tienda.inventario.modelo.DetalleSalida;
import tienda.inventario.modelo.Empresa;
import tienda.inventario.repositorio.ProductoRepositorio;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfServicio {

    private static final Logger log = LoggerFactory.getLogger(PdfServicio.class);

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private IEmpresaServicio empresaServicio;

    public byte[] generarKardexPdf(List<KardexResponseDTO> movimientos, Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Obtener datos de la empresa
        Empresa empresa = empresaServicio.obtenerConfiguracion();

        // Título
        document.add(new Paragraph("Reporte Kardex de Inventario")
                .setFont(boldFont).setFontSize(18).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        document.add(new Paragraph(empresa.getNombreEmpresa())
                .setFont(boldFont).setFontSize(12).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        if (empresa.getRuc() != null && !empresa.getRuc().trim().isEmpty()) {
            document.add(new Paragraph("RUC: " + empresa.getRuc())
                    .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        if (empresa.getDireccion() != null && !empresa.getDireccion().trim().isEmpty()) {
            document.add(new Paragraph(empresa.getDireccion())
                    .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        document.add(new Paragraph("Fecha de Generación: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        document.add(new Paragraph("\n"));

        // Información del Producto
        if (idProducto != null) {
            Producto producto = productoRepositorio.findById(idProducto).orElse(null);
            if (producto != null) {
                document.add(new Paragraph("Producto: " + producto.getNombreProducto())
                        .setFont(boldFont).setFontSize(12));
                document.add(new Paragraph("Código: " + producto.getIdProducto())
                        .setFont(font).setFontSize(10));
                document.add(new Paragraph("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria().getNombre() : "N/A"))
                        .setFont(font).setFontSize(10));
            }
        }

        // Rango de Fechas
        String rangoFechas = "";
        if (fechaInicio != null && fechaFin != null) {
            rangoFechas = "Del " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                          " al " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            rangoFechas = "Todos los movimientos";
        }
        document.add(new Paragraph("Período: " + rangoFechas)
                .setFont(font).setFontSize(10));
        document.add(new Paragraph("\n"));

        // Tabla de Movimientos - anchos optimizados para mejor alineación
        float[] columnWidths = {1.2f, 1.0f, 1.8f, 0.9f, 1.0f, 1.1f, 0.9f, 0.9f, 1.2f};
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
        table.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        // Encabezados de la tabla - nombres completos y descriptivos
        table.addHeaderCell(new Cell().add(new Paragraph("Fecha y Hora").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Tipo de Movimiento").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Referencia del Documento").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Precio Unitario").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Valor Total").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock Anterior").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock Actual").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Observaciones").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        // Filas de datos
        for (KardexResponseDTO mov : movimientos) {
            table.addCell(new Cell().add(new Paragraph(mov.getFechaMovimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(mov.getTipoMovimiento()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(mov.getReferenciaDocumento() != null ? mov.getReferenciaDocumento() : "").setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(mov.getCantidad())).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getPrecioUnitario() != null ? mov.getPrecioUnitario().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00").setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getValorTotal() != null ? mov.getValorTotal().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00").setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(mov.getStockAnterior() != null ? mov.getStockAnterior() : 0)).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(mov.getStockActual() != null ? mov.getStockActual() : 0)).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getObservaciones() != null ? mov.getObservaciones() : "").setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT));
        }

        document.add(table);

        // Totales/resumen al pie cuando hay un producto filtrado
        if (idProducto != null) {
            int totalEntradas = 0;
            int totalSalidas = 0;
            java.math.BigDecimal totalEntradasVal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalSalidasVal = java.math.BigDecimal.ZERO;
            Integer saldoInicial = null;
            Integer stockFinal = null;
            java.math.BigDecimal costoPromFinal = java.math.BigDecimal.ZERO;

            if (!movimientos.isEmpty()) {
                // Tomar el stockAnterior del primer movimiento listado como aproximación de saldo inicial del periodo
                saldoInicial = movimientos.get(movimientos.size() - 1).getStockAnterior();
            }

            for (KardexResponseDTO m : movimientos) {
                if ("SALIDA".equalsIgnoreCase(m.getTipoMovimiento())) {
                    totalSalidas += m.getCantidad();
                    if (m.getValorTotal() != null) totalSalidasVal = totalSalidasVal.add(m.getValorTotal());
                } else {
                    totalEntradas += m.getCantidad();
                    if (m.getValorTotal() != null) totalEntradasVal = totalEntradasVal.add(m.getValorTotal());
                }
                if (m.getCostoPromedioActual() != null) costoPromFinal = m.getCostoPromedioActual();
                stockFinal = m.getStockActual();
            }

            document.add(new Paragraph("\n"));
            Table resumen = new Table(new float[]{2f, 1.2f, 1.6f, 1.6f, 1.8f}).useAllAvailableWidth();
            resumen.addHeaderCell(new Cell().add(new Paragraph("Concepto").setFont(boldFont)));
            resumen.addHeaderCell(new Cell().add(new Paragraph("Saldo Inicial").setFont(boldFont)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            resumen.addHeaderCell(new Cell().add(new Paragraph("Entradas (Cant/Valor)").setFont(boldFont)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            resumen.addHeaderCell(new Cell().add(new Paragraph("Salidas (Cant/Valor)").setFont(boldFont)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            resumen.addHeaderCell(new Cell().add(new Paragraph("Stock Final / Costo Final").setFont(boldFont)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

            resumen.addCell(new Cell().add(new Paragraph(idProducto != null ? ("Producto #" + idProducto) : "Todos")));
            resumen.addCell(new Cell().add(new Paragraph(String.valueOf(saldoInicial != null ? saldoInicial : 0))).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            resumen.addCell(new Cell().add(new Paragraph(totalEntradas + " / " + totalEntradasVal.setScale(2, java.math.RoundingMode.HALF_UP))).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            resumen.addCell(new Cell().add(new Paragraph(totalSalidas + " / " + totalSalidasVal.setScale(2, java.math.RoundingMode.HALF_UP))).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            java.math.BigDecimal costoTotalFinal = (stockFinal != null ? new java.math.BigDecimal(stockFinal) : java.math.BigDecimal.ZERO)
                    .multiply(costoPromFinal != null ? costoPromFinal : java.math.BigDecimal.ZERO)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            resumen.addCell(new Cell().add(new Paragraph((stockFinal != null ? stockFinal : 0) + " / " + costoTotalFinal)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

            document.add(resumen);
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPdfDesdeHtml(String htmlContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, baos);
        return baos.toByteArray();
    }

    public byte[] generarBoletaVenta(Salida salida) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Obtener datos de la empresa
        Empresa empresa = empresaServicio.obtenerConfiguracion();

        // Encabezado de la boleta
        document.add(new Paragraph("BOLETA DE VENTA")
                .setFont(boldFont).setFontSize(20).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        document.add(new Paragraph(empresa.getNombreEmpresa())
                .setFont(boldFont).setFontSize(14).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        
        if (empresa.getRuc() != null && !empresa.getRuc().trim().isEmpty()) {
            document.add(new Paragraph("RUC: " + empresa.getRuc())
                    .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        
        if (empresa.getDireccion() != null && !empresa.getDireccion().trim().isEmpty()) {
            document.add(new Paragraph(empresa.getDireccion())
                    .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        
        if (empresa.getTelefono() != null && !empresa.getTelefono().trim().isEmpty()) {
            document.add(new Paragraph("Tel: " + empresa.getTelefono())
                    .setFont(font).setFontSize(10).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        
        document.add(new Paragraph("\n"));

        // Información de la venta
        document.add(new Paragraph("BOLETA N°: " + salida.getIdSalida())
                .setFont(boldFont).setFontSize(12));
        document.add(new Paragraph("FECHA: " + salida.getFechaSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(font).setFontSize(10));
        document.add(new Paragraph("TIPO DE VENTA: " + (salida.getTipoVenta() != null ? salida.getTipoVenta() : "CONTADO"))
                .setFont(font).setFontSize(10));

        // Información del cliente
        if (salida.getCliente() != null) {
            document.add(new Paragraph("CLIENTE: " + salida.getCliente().getNombres() + " " + salida.getCliente().getApellidos())
                    .setFont(font).setFontSize(10));
            document.add(new Paragraph("DNI: " + salida.getCliente().getDni())
                    .setFont(font).setFontSize(10));
        } else {
            document.add(new Paragraph("CLIENTE: CONSUMIDOR FINAL")
                    .setFont(font).setFontSize(10));
        }

        // Para ventas a crédito, mostrar fecha de pago
        if ("CREDITO".equals(salida.getTipoVenta()) && salida.getFechaPagoCredito() != null) {
            document.add(new Paragraph("FECHA DE PAGO: " + salida.getFechaPagoCredito().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(font).setFontSize(10));
        }

        document.add(new Paragraph("\n"));

        // Tabla de productos
        float[] columnWidths = {3, 1, 1, 2}; // Producto, Cantidad, Precio, Subtotal
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        // Encabezados de la tabla
        table.addHeaderCell(new Cell().add(new Paragraph("PRODUCTO").setFont(boldFont).setFontSize(9))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("CANT.").setFont(boldFont).setFontSize(9))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("P.UNIT.").setFont(boldFont).setFontSize(9))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("SUBTOTAL").setFont(boldFont).setFontSize(9))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        // Filas de productos
        for (DetalleSalida detalle : salida.getDetalles()) {
            table.addCell(new Cell().add(new Paragraph(detalle.getProducto().getNombreProducto())
                    .setFont(font).setFontSize(8))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(detalle.getCantidad()))
                    .setFont(font).setFontSize(8))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", detalle.getPrecioUnitario()))
                    .setFont(font).setFontSize(8))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", detalle.getSubtotal()))
                    .setFont(font).setFontSize(8))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        // Total
        document.add(new Paragraph("TOTAL: S/ " + String.format("%.2f", salida.getTotalSalida()))
                .setFont(boldFont).setFontSize(14)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

        document.add(new Paragraph("\n"));

        // Pie de página
        document.add(new Paragraph("¡Gracias por su compra!")
                .setFont(font).setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        document.add(new Paragraph("Vuelva pronto")
                .setFont(font).setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        document.close();
        return baos.toByteArray();
    }

    public byte[] generarTicketSalida80mm(Salida salida) throws IOException {
        Empresa empresa = empresaServicio.obtenerConfiguracion();
        String nombreEmpresa = empresa != null && empresa.getNombreEmpresa() != null ? empresa.getNombreEmpresa() : "";
        String ruc = empresa != null && empresa.getRuc() != null ? empresa.getRuc() : "";
        String direccion = empresa != null && empresa.getDireccion() != null ? empresa.getDireccion() : "";
        String telefono = empresa != null && empresa.getTelefono() != null ? empresa.getTelefono() : "";

        StringBuilder items = new StringBuilder();
        if (salida.getDetalles() != null) {
            for (DetalleSalida d : salida.getDetalles()) {
                String nombre = d.getProducto() != null ? d.getProducto().getNombreProducto() : "";
                String cant = String.valueOf(d.getCantidad());
                String pu = formatMoney(d.getPrecioUnitario());
                String sub = formatMoney(d.getSubtotal());
                items.append("<tr>"
                        + "<td>" + nombre + "</td>"
                        + "<td class='right'>" + cant + "</td>"
                        + "<td class='right'>" + pu + "</td>"
                        + "<td class='right'>" + sub + "</td>"
                        + "</tr>");
            }
        }

        String nombreCliente = "CONSUMIDOR FINAL";
        String dniCliente = "";
        if (salida.getCliente() != null) {
            String nombres = salida.getCliente().getNombres() != null ? salida.getCliente().getNombres() : "";
            String apellidos = salida.getCliente().getApellidos() != null ? salida.getCliente().getApellidos() : "";
            nombreCliente = (nombres + " " + apellidos).trim();
            dniCliente = salida.getCliente().getDni() != null ? salida.getCliente().getDni() : "";
        }

        String tipoVenta = salida.getTipoVenta() != null ? salida.getTipoVenta() : "CONTADO";
        String fechaPago = ("CREDITO".equalsIgnoreCase(tipoVenta) && salida.getFechaPagoCredito() != null)
                ? salida.getFechaPagoCredito().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null;

        String fechaSalida;
        if (salida.getFechaSalida() == null) {
            fechaSalida = "";
        } else {
            try {
                LocalDate f = salida.getFechaSalida();
                fechaSalida = f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                fechaSalida = salida.getFechaSalida().toString();
            }
        }

        String html = "" +
                "<html><head><meta charset='UTF-8'/>" +
                "<style>" +
                /* margin 0: el ancho lo fija el PageSize (80mm) */
                "@page { margin: 0; }" +
                "html, body { margin: 0; padding: 0; }" +
                "body { font-family: monospace; color: #000; }" +
                ".center{text-align:center;} .right{text-align:right;} .row{display:flex;justify-content:space-between;}" +
                ".muted{color:#333;font-size:11px;} .bold{font-weight:700;} .hr{border-top:1px dashed #000;margin:6px 0;}" +
                "table{width:100%;font-size:11px;border-collapse:collapse; page-break-inside: avoid;} thead, tr, td{ page-break-inside: avoid; } th,td{padding:2px 0;}" +
                "</style></head><body>" +
                "<div style='width:100%;padding:3mm;box-sizing:border-box'>" +
                "<div class='center' style='margin-bottom:4px'>" +
                "<div class='bold' style='font-size:14px'>" + nombreEmpresa + "</div>" +
                (ruc.isEmpty() ? "" : ("<div>RUC: " + ruc + "</div>")) +
                (direccion.isEmpty() ? "" : ("<div class='muted'>" + direccion + "</div>")) +
                ((telefono.isEmpty()) ? "" : ("<div class='muted'>" + telefono + "</div>")) +
                "</div>" +
                "<div class='hr'></div>" +
                "<div style='font-size:11px'>" +
                "<div class='row'><span>ID Salida:</span><span class='bold'>#" + salida.getIdSalida() + "</span></div>" +
                "<div class='row'><span>Fecha:</span><span>" + fechaSalida + "</span></div>" +
                "<div class='row'><span>Tipo:</span><span class='bold'>" + ("CREDITO".equalsIgnoreCase(tipoVenta) ? "Crédito" : "Contado") + "</span></div>" +
                (fechaPago != null ? ("<div class='row'><span>Pago (crédito):</span><span>" + fechaPago + "</span></div>") : "") +
                "</div>" +
                "<div style='margin-top:6px;font-size:11px'><div class='bold'>Cliente</div>" + nombreCliente +
                (dniCliente.isEmpty() ? "" : ("<div class='muted'>DNI: " + dniCliente + "</div>")) +
                "</div>" +
                "<div class='hr'></div>" +
                "<table>" +
                "<thead><tr><th style='text-align:left'>Producto</th><th class='right' style='width:20px'>Cant</th><th class='right' style='width:32px'>P.U.</th><th class='right' style='width:40px'>Subt</th></tr></thead>" +
                "<tbody>" + items + "</tbody>" +
                "</table>" +
                "<div class='hr'></div>" +
                "<div class='row' style='font-size:12px'><span class='bold'>TOTAL</span><span class='bold'>" + formatMoney(salida.getTotalSalida()) + "</span></div>" +
                ("CREDITO".equalsIgnoreCase(tipoVenta) ? "<div class='center muted' style='margin-top:8px'>Venta a crédito. Sujeto a pago en la fecha indicada.</div>" : "<div class='center muted' style='margin-top:8px'>Venta al contado.</div>") +
                "<div class='center' style='margin-top:8px;font-size:11px'>¡Gracias por su compra!</div>" +
                "</div>" +
                "</body></html>";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Página tipo rollo: ancho fijo 80mm y alto dinámico estimado según contenido
            float widthPt = 80f * 72f / 25.4f;  // 80mm en puntos
            int numItems = salida.getDetalles() != null ? salida.getDetalles().size() : 0;
            // Estimación de alto (mm) más generosa para evitar corte en 2 páginas
            float headerMm = 60f;      // cabecera + datos cliente
            float perItemMm = 12f;     // alto por fila de item (aprox)
            float footerMm = 35f;      // totales + nota
            float heightMm = headerMm + (numItems * perItemMm) + footerMm;
            // Asegura mínimo 80mm, sin límite superior para no forzar salto
            heightMm = Math.max(80f, heightMm);
            float heightPt = heightMm * 72f / 25.4f;
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(new PageSize(widthPt, heightPt));
            ConverterProperties props = new ConverterProperties();
            HtmlConverter.convertToPdf(html, pdfDoc, props);
            pdfDoc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("Error convirtiendo HTML a PDF del ticket 80mm", ex);
            // Fallback: PDF básico con información esencial
            baos.reset();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            float widthPt = 80f * 72f / 25.4f;
            float heightPt = 120f * 72f / 25.4f; // fallback 120mm de alto
            pdf.setDefaultPageSize(new PageSize(widthPt, heightPt));
            Document document = new Document(pdf);
            document.add(new Paragraph(nombreEmpresa).setBold());
            if (!ruc.isEmpty()) document.add(new Paragraph("RUC: " + ruc));
            if (!direccion.isEmpty()) document.add(new Paragraph(direccion));
            if (!telefono.isEmpty()) document.add(new Paragraph(telefono));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("ID: #" + salida.getIdSalida()));
            document.add(new Paragraph("Fecha: " + fechaSalida));
            document.add(new Paragraph("Tipo: " + ("CREDITO".equalsIgnoreCase(tipoVenta) ? "Crédito" : "Contado")));
            if (fechaPago != null) document.add(new Paragraph("Pago (crédito): " + fechaPago));
            document.add(new Paragraph("Cliente: " + nombreCliente));
            if (!dniCliente.isEmpty()) document.add(new Paragraph("DNI: " + dniCliente));
            document.add(new Paragraph("Total: " + formatMoney(salida.getTotalSalida())).setBold());
            document.add(new Paragraph("Gracias por su compra!").setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.close();
            return baos.toByteArray();
        }
    }

    private String formatMoney(Object value) {
        try {
            BigDecimal bd;
            if (value == null) return "S/ 0.00";
            if (value instanceof BigDecimal) {
                bd = (BigDecimal) value;
            } else {
                bd = new BigDecimal(String.valueOf(value));
            }
            return "S/ " + bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return "S/ 0.00";
        }
    }

    /**
     * Generar reporte PDF de caja diaria
     */
    public byte[] generarReporteCaja(tienda.inventario.modelo.CajaDiaria caja, java.util.List<tienda.inventario.modelo.MovimientoCaja> movimientos) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // Obtener datos de empresa
            Empresa empresa = empresaServicio.obtenerConfiguracion();

            // Fuentes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Título
            Paragraph titulo = new Paragraph("REPORTE DE CAJA DIARIA")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titulo);

            // Información de empresa
            Paragraph empresaInfo = new Paragraph(empresa.getNombreEmpresa())
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(empresaInfo);

            if (empresa.getDireccion() != null && !empresa.getDireccion().isEmpty()) {
                Paragraph direccion = new Paragraph(empresa.getDireccion())
                        .setFont(fontNormal)
                        .setFontSize(10)
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(direccion);
            }

            // Información de la caja
            Table infoTable = new Table(2).useAllAvailableWidth();
            infoTable.addCell(createCell("Fecha:", fontBold, true));
            infoTable.addCell(createCell(caja.getFecha().toString(), fontNormal, false));
            infoTable.addCell(createCell("Estado:", fontBold, true));
            infoTable.addCell(createCell(caja.getEstado().toString(), fontNormal, false));
            infoTable.addCell(createCell("Monto Apertura:", fontBold, true));
            infoTable.addCell(createCell(formatMoney(caja.getMontoApertura()), fontNormal, false));
            infoTable.addCell(createCell("Total Ingresos:", fontBold, true));
            infoTable.addCell(createCell(formatMoney(caja.getTotalIngresos()), fontNormal, false));
            infoTable.addCell(createCell("Total Egresos:", fontBold, true));
            infoTable.addCell(createCell(formatMoney(caja.getTotalEgresos()), fontNormal, false));
            infoTable.addCell(createCell("Saldo Actual:", fontBold, true));
            infoTable.addCell(createCell(formatMoney(caja.getSaldoActual()), fontNormal, false));
            
            if (caja.getFechaApertura() != null) {
                infoTable.addCell(createCell("Hora Apertura:", fontBold, true));
                infoTable.addCell(createCell(caja.getFechaApertura().toString(), fontNormal, false));
            }
            
            if (caja.getFechaCierre() != null) {
                infoTable.addCell(createCell("Hora Cierre:", fontBold, true));
                infoTable.addCell(createCell(caja.getFechaCierre().toString(), fontNormal, false));
            }

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Tabla de movimientos
            if (movimientos != null && !movimientos.isEmpty()) {
                Paragraph movimientosTitulo = new Paragraph("MOVIMIENTOS DE CAJA")
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setMarginBottom(10);
                document.add(movimientosTitulo);

                Table movimientosTable = new Table(5).useAllAvailableWidth();
                
                // Encabezados
                movimientosTable.addCell(createCell("Hora", fontBold, true));
                movimientosTable.addCell(createCell("Tipo", fontBold, true));
                movimientosTable.addCell(createCell("Descripción", fontBold, true));
                movimientosTable.addCell(createCell("Monto", fontBold, true));
                movimientosTable.addCell(createCell("Usuario", fontBold, true));

                // Datos
                for (tienda.inventario.modelo.MovimientoCaja movimiento : movimientos) {
                    movimientosTable.addCell(createCell(movimiento.getFechaMovimiento().toString(), fontNormal, false));
                    movimientosTable.addCell(createCell(movimiento.getTipoMovimiento().toString(), fontNormal, false));
                    movimientosTable.addCell(createCell(movimiento.getDescripcion() != null ? movimiento.getDescripcion() : "", fontNormal, false));
                    movimientosTable.addCell(createCell(formatMoney(movimiento.getMonto()), fontNormal, false));
                    movimientosTable.addCell(createCell(movimiento.getUsuarioRegistro() != null ? movimiento.getUsuarioRegistro() : "", fontNormal, false));
                }

                document.add(movimientosTable);
            }

            // Resumen final
            document.add(new Paragraph("\n"));
            Paragraph resumen = new Paragraph("RESUMEN FINAL")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(resumen);

            Table resumenTable = new Table(2).useAllAvailableWidth();
            resumenTable.addCell(createCell("Total de Movimientos:", fontBold, true));
            resumenTable.addCell(createCell(String.valueOf(movimientos != null ? movimientos.size() : 0), fontNormal, false));
            resumenTable.addCell(createCell("Saldo Final:", fontBold, true));
            resumenTable.addCell(createCell(formatMoney(caja.getSaldoActual()), fontBold, false));

            document.add(resumenTable);

            // Pie de página
            document.add(new Paragraph("\n"));
            Paragraph pie = new Paragraph("Reporte generado el " + java.time.LocalDateTime.now().toString())
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(pie);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar reporte de caja: ", e);
            throw new RuntimeException("Error al generar reporte de caja", e);
        }
    }

    private Cell createCell(String text, PdfFont font, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font));
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
        }
        return cell;
    }
}
