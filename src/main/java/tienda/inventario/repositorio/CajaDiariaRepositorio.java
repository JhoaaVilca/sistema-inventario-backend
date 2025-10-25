package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.CajaDiaria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CajaDiariaRepositorio extends JpaRepository<CajaDiaria, Long> {

    /**
     * Buscar cualquier caja abierta (sin importar la fecha)
     * Ordenadas por fecha de apertura descendente para obtener la más reciente primero
     */
    @Query("SELECT c FROM CajaDiaria c WHERE c.estado = 'ABIERTA' ORDER BY c.fechaApertura DESC")
    Optional<CajaDiaria> findCajaAbiertaActual();

    /**
     * Verificar si existe alguna caja abierta (sin importar la fecha)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM CajaDiaria c WHERE c.estado = 'ABIERTA'")
    boolean existsCajaAbierta();

    /**
     * Buscar la caja abierta de una fecha específica
     */
    @Query("SELECT c FROM CajaDiaria c WHERE c.fecha = :fecha AND c.estado = 'ABIERTA'")
    Optional<CajaDiaria> findCajaAbiertaByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Verificar si existe una caja abierta en una fecha específica
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM CajaDiaria c WHERE c.fecha = :fecha AND c.estado = 'ABIERTA'")
    boolean existsCajaAbiertaByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Buscar caja por fecha (puede estar abierta o cerrada)
     */
    Optional<CajaDiaria> findByFecha(LocalDate fecha);

    /**
     * Listar cajas por rango de fechas
     */
    @Query("SELECT c FROM CajaDiaria c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fecha DESC")
    List<CajaDiaria> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                       @Param("fechaFin") LocalDate fechaFin);

    /**
     * Buscar las últimas cajas cerradas, ordenadas por fecha de cierre descendente
     */
    @Query("SELECT c FROM CajaDiaria c WHERE c.estado = 'CERRADA' ORDER BY c.fechaCierre DESC")
    List<CajaDiaria> findUltimasCajasCerradas();

    /**
     * Obtener resumen de cajas por mes y año
     */
    @Query("SELECT c FROM CajaDiaria c WHERE YEAR(c.fecha) = :año AND MONTH(c.fecha) = :mes ORDER BY c.fecha DESC")
    List<CajaDiaria> findByMesAndAño(@Param("mes") int mes, @Param("año") int año);
}

