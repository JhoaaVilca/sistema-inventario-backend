package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.Lote;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoteRepositorio extends JpaRepository<Lote, Long> {

    // Buscar lotes por producto
    @Query("SELECT l FROM Lote l WHERE l.detalleEntrada.producto.idProducto = :idProducto AND l.estado = 'Activo' AND COALESCE(l.cantidadDisponible, 0) > 0 ORDER BY l.fechaVencimiento ASC")
    List<Lote> findByProductoIdProducto(@Param("idProducto") Long idProducto);

    // Buscar lotes próximos a vencer (próximos 30 días)
    @Query("SELECT l FROM Lote l WHERE l.fechaVencimiento BETWEEN :fechaActual AND :fechaLimite AND l.estado = 'Activo' ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesProximosAVencer(@Param("fechaActual") LocalDate fechaActual, @Param("fechaLimite") LocalDate fechaLimite);

    // Buscar lotes vencidos
    @Query("SELECT l FROM Lote l WHERE l.fechaVencimiento < :fechaActual AND l.estado = 'Activo' ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesVencidos(@Param("fechaActual") LocalDate fechaActual);

    // Buscar lotes por detalle de entrada
    Lote findByDetalleEntradaIdDetalle(Long idDetalle);

    // Obtener stock total de un producto (suma de cantidades de lotes activos)
    @Query("SELECT COALESCE(SUM(COALESCE(l.cantidadDisponible, 0)), 0) FROM Lote l WHERE l.detalleEntrada.producto.idProducto = :idProducto AND l.estado = 'Activo'")
    Integer getStockTotalPorProducto(@Param("idProducto") Long idProducto);

    // Buscar lotes por producto ordenados por fecha de entrada descendente
    @Query("SELECT l FROM Lote l WHERE l.detalleEntrada.producto.idProducto = :idProducto ORDER BY l.fechaEntrada DESC")
    List<Lote> findByDetalleEntradaProductoIdProductoOrderByFechaEntradaDesc(@Param("idProducto") Long idProducto);
}
