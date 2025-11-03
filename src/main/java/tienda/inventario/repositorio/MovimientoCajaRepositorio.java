package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.MovimientoCaja;
import tienda.inventario.modelo.CajaDiaria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoCajaRepositorio extends JpaRepository<MovimientoCaja, Long> {

    /**
     * Buscar movimientos por caja
     */
    List<MovimientoCaja> findByCajaDiariaOrderByFechaMovimientoDesc(CajaDiaria cajaDiaria);

    /**
     * Buscar movimientos por caja y tipo
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE m.cajaDiaria = :caja AND m.tipoMovimiento = :tipo ORDER BY m.fechaMovimiento DESC")
    List<MovimientoCaja> findByCajaDiariaAndTipoMovimiento(@Param("caja") CajaDiaria caja, 
                                                          @Param("tipo") MovimientoCaja.TipoMovimiento tipo);

    /**
     * Buscar movimientos por fecha
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE DATE(m.fechaMovimiento) = :fecha ORDER BY m.fechaMovimiento DESC")
    List<MovimientoCaja> findByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Buscar movimientos por rango de fechas
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE DATE(m.fechaMovimiento) BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoCaja> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                           @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcular total de ingresos por caja
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.cajaDiaria = :caja AND m.tipoMovimiento = 'INGRESO'")
    Double calcularTotalIngresosPorCaja(@Param("caja") CajaDiaria caja);

    /**
     * Calcular total de egresos por caja
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.cajaDiaria = :caja AND m.tipoMovimiento = 'EGRESO'")
    Double calcularTotalEgresosPorCaja(@Param("caja") CajaDiaria caja);

    /**
     * Buscar movimientos por salida (ventas al contado)
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE m.salida.id = :idSalida")
    List<MovimientoCaja> findBySalidaId(@Param("idSalida") Long idSalida);

    /**
     * Buscar movimientos por entrada (compras)
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE m.entrada.id = :idEntrada")
    List<MovimientoCaja> findByEntradaId(@Param("idEntrada") Long idEntrada);

    /**
     * Buscar movimientos por pago de crédito
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE m.pagoCredito.id = :idPagoCredito")
    List<MovimientoCaja> findByPagoCreditoId(@Param("idPagoCredito") Long idPagoCredito);

    /**
     * Obtener movimientos del día actual
     */
    @Query("SELECT m FROM MovimientoCaja m WHERE CAST(m.fechaMovimiento AS date) = CURRENT_DATE ORDER BY m.fechaMovimiento DESC")
    List<MovimientoCaja> findMovimientosDelDia();

    /**
     * Contar movimientos por caja
     */
    @Query("SELECT COUNT(m) FROM MovimientoCaja m WHERE m.cajaDiaria = :caja")
    Long countByCajaDiaria(@Param("caja") CajaDiaria caja);
}




