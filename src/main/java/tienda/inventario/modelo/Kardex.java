package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKardex;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(nullable = false, length = 50)
    private String tipoMovimiento; // ENTRADA, SALIDA, AJUSTE

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario; // Precio al que entra o sale el producto

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal; // Cantidad * Precio Unitario

    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "costo_promedio_anterior", precision = 10, scale = 2)
    private BigDecimal costoPromedioAnterior;

    @Column(name = "costo_promedio_actual", precision = 10, scale = 2)
    private BigDecimal costoPromedioActual;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "referencia_documento", length = 100)
    private String referenciaDocumento; // Ej: N° Factura de Entrada, N° Venta de Salida

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro; // Usuario que realizó el movimiento

    @PrePersist
    public void prePersist() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
        if (precioUnitario == null) {
            precioUnitario = BigDecimal.ZERO;
        }
        if (valorTotal == null) {
            valorTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        if (costoPromedioAnterior == null) {
            costoPromedioAnterior = BigDecimal.ZERO;
        }
        if (costoPromedioActual == null) {
            costoPromedioActual = BigDecimal.ZERO;
        }
    }

    // Método para calcular el nuevo costo promedio ponderado
    public static BigDecimal calcularCostoPromedioPonderado(
            BigDecimal stockAnterior, BigDecimal precioAnterior,
            BigDecimal precioEntrada, Integer cantidadEntrada) {

        if (stockAnterior.add(BigDecimal.valueOf(cantidadEntrada)).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Evitar división por cero si no hay stock
        }

        BigDecimal totalStock = stockAnterior.add(BigDecimal.valueOf(cantidadEntrada));
        BigDecimal totalValor = (precioAnterior.multiply(stockAnterior))
                              .add(precioEntrada.multiply(BigDecimal.valueOf(cantidadEntrada)));

        return totalValor.divide(totalStock, 2, java.math.RoundingMode.HALF_UP);
    }
}

