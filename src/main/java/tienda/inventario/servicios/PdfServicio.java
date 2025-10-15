package tienda.inventario.servicios;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.dto.KardexResponseDTO;
import tienda.inventario.modelo.Producto;
import tienda.inventario.repositorio.ProductoRepositorio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.math.BigDecimal;

@Service
public class PdfServicio {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    public byte[] generarKardexPdf(List<KardexResponseDTO> movimientos, Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Título
        document.add(new Paragraph("Reporte Kardex de Inventario")
                .setFont(boldFont).setFontSize(18).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        document.add(new Paragraph("Comercial Yoli")
                .setFont(font).setFontSize(12).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
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
            rangoFechas = "Desde: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                          " Hasta: " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            rangoFechas = "Todos los movimientos";
        }
        document.add(new Paragraph("Período: " + rangoFechas)
                .setFont(font).setFontSize(10));
        document.add(new Paragraph("\n"));

        // Tabla de Movimientos
        float[] columnWidths = {1, 2, 2, 1, 1, 1, 1, 1, 2}; // Ajustar anchos de columna
        Table table = new Table(columnWidths);
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
        table.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        // Encabezados de la tabla
        table.addHeaderCell(new Cell().add(new Paragraph("Fecha").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Tipo").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Referencia").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("P. Unit.").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("V. Total").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Stock Actual").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Costo Prom.").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Observaciones").setFont(boldFont).setFontSize(8)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        // Filas de datos
        for (KardexResponseDTO mov : movimientos) {
            table.addCell(new Cell().add(new Paragraph(mov.getFechaMovimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(mov.getTipoMovimiento()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(mov.getReferenciaDocumento()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(mov.getCantidad())).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getPrecioUnitario().setScale(2, java.math.RoundingMode.HALF_UP).toString()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getValorTotal().setScale(2, java.math.RoundingMode.HALF_UP).toString()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(mov.getStockActual())).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getCostoPromedioActual().setScale(2, java.math.RoundingMode.HALF_UP).toString()).setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(mov.getObservaciones() != null ? mov.getObservaciones() : "").setFont(font).setFontSize(7)).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT));
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] generarPdfDesdeHtml(String htmlContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, baos);
        return baos.toByteArray();
    }
}
