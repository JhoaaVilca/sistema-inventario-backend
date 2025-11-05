package tienda.inventario.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KardexResumenDTO {
    private Long idProducto;
    private String nombreProducto;
    private Integer saldoInicial; // stock al inicio del periodo
    private Integer totalEntradasCantidad;
    private Integer totalSalidasCantidad;
    private Integer stockFinal; // saldo inicial + entradas - salidas

    private BigDecimal totalEntradasValor; // suma de valorTotal de ENTRADA/AJUSTE(+)
    private BigDecimal totalSalidasValor;  // suma de valorTotal de SALIDA
    private BigDecimal costoPromedioFinal; // costo promedio al cierre (si hay)
    private BigDecimal costoTotalFinal;    // stockFinal * costoPromedioFinal

    // Opcional: ganancia estimada (si se integra con ventas)
    private BigDecimal gananciaEstimada; // puede ser null si no se puede calcular
}



