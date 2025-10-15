package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tienda.inventario.modelo.Kardex;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public interface KardexRepositorio extends JpaRepository<Kardex, Long> {

    // Obtener movimientos de Kardex por producto y rango de fechas (ordenado por Pageable)
    Page<Kardex> findByProductoIdProductoAndFechaMovimientoBetween(
            Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Obtener movimientos de Kardex por producto (ordenado por Pageable)
    Page<Kardex> findByProductoIdProducto(Long idProducto, Pageable pageable);

    // Obtener el último movimiento de un producto
    List<Kardex> findTop1ByProductoIdProductoOrderByFechaMovimientoDesc(Long idProducto);

    // Obtener movimientos de Kardex por rango de fechas (general, ordenado por Pageable)
    Page<Kardex> findByFechaMovimientoBetween(
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Obtener todos los movimientos de Kardex (ordenado por Pageable)
    Page<Kardex> findAll(Pageable pageable);

    // Obtener el stock actual de un producto (último registro de Kardex)
    @Query("SELECT k.stockActual FROM Kardex k WHERE k.producto.idProducto = :idProducto ORDER BY k.fechaMovimiento DESC LIMIT 1")
    Integer findCurrentStockByProductId(@Param("idProducto") Long idProducto);

    // Obtener el costo promedio actual de un producto (último registro de Kardex)
    @Query("SELECT k.costoPromedioActual FROM Kardex k WHERE k.producto.idProducto = :idProducto ORDER BY k.fechaMovimiento DESC LIMIT 1")
    BigDecimal findCurrentAverageCostByProductId(@Param("idProducto") Long idProducto);
}
