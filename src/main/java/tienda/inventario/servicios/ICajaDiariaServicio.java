package tienda.inventario.servicios;

import tienda.inventario.modelo.CajaDiaria;
import tienda.inventario.modelo.MovimientoCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICajaDiariaServicio {

    /**
     * Abrir caja del día
     */
    CajaDiaria abrirCaja(BigDecimal montoApertura, String usuario, String observaciones);

    /**
     * Cerrar caja del día
     */
    CajaDiaria cerrarCaja(Long idCaja, String usuario, String observaciones);

    /**
     * Obtener caja abierta del día actual
     */
    Optional<CajaDiaria> obtenerCajaAbierta();

    /**
     * Obtener caja por fecha
     */
    Optional<CajaDiaria> obtenerCajaPorFecha(LocalDate fecha);

    /**
     * Verificar si existe caja abierta
     */
    boolean existeCajaAbierta();

    /**
     * Obtener resumen de caja
     */
    CajaDiaria obtenerResumenCaja(Long idCaja);

    /**
     * Listar cajas por rango de fechas
     */
    List<CajaDiaria> listarCajasPorRango(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtener movimientos de una caja específica
     */
    List<MovimientoCaja> obtenerMovimientosPorCaja(Long idCaja);

    /**
     * Listar cajas con paginación
     */
    Page<CajaDiaria> listarCajas(Pageable pageable);

    /**
     * Registrar movimiento de caja
     */
    MovimientoCaja registrarMovimiento(Long idCaja, MovimientoCaja.TipoMovimiento tipo, 
                                     BigDecimal monto, String descripcion, String usuario, 
                                     String referenciaDocumento);

    /**
     * Registrar ingreso por venta al contado
     */
    MovimientoCaja registrarIngresoVenta(Long idCaja, BigDecimal monto, String descripcion, 
                                        String usuario, Long idSalida);

    /**
     * Registrar egreso por compra
     */
    MovimientoCaja registrarEgresoCompra(Long idCaja, BigDecimal monto, String descripcion, 
                                         String usuario, Long idEntrada);

    /**
     * Registrar ingreso por pago de crédito
     */
    MovimientoCaja registrarIngresoPagoCredito(Long idCaja, BigDecimal monto, String descripcion, 
                                              String usuario, Long idPagoCredito);

    /**
     * Registrar egreso manual (gasto)
     */
    MovimientoCaja registrarEgresoManual(Long idCaja, BigDecimal monto, String descripcion, 
                                        String usuario, String observaciones);

    /**
     * Obtener movimientos de caja
     */
    List<MovimientoCaja> obtenerMovimientosCaja(Long idCaja);

    /**
     * Calcular totales de caja
     */
    void calcularTotalesCaja(Long idCaja);

    /**
     * Obtener historial de cajas
     */
    List<CajaDiaria> obtenerHistorialCajas(int dias);

    /**
     * Generar reporte de caja
     */
    byte[] generarReporteCaja(Long idCaja);
}

