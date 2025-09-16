package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tienda.inventario.modelo.Entrada;

import java.time.LocalDate;
import java.util.List;

public interface EntradaRepositorio extends JpaRepository<Entrada, Long> {
    // Buscar por proveedor
    @Query("SELECT e FROM Entrada e WHERE e.proveedor.idProveedor = :idProveedor")
    List<Entrada> findByProveedorId(Long idProveedor);

    // Buscar por fecha exacta
    List<Entrada> findByFechaEntrada(LocalDate fechaEntrada);

    // Buscar por rango de fechas
    List<Entrada> findByFechaEntradaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT e FROM Entrada e WHERE e.proveedor.idProveedor = :idProveedor AND e.fechaEntrada BETWEEN :fechaInicio AND :fechaFin")
    List<Entrada> findByProveedorIdAndFechaEntradaBetween(Long idProveedor, LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar por número de factura (búsqueda parcial)
    @Query("SELECT e FROM Entrada e WHERE e.numeroFactura LIKE %:numeroFactura%")
    List<Entrada> findByNumeroFacturaContaining(String numeroFactura);

    // Buscar por número de factura exacto
    List<Entrada> findByNumeroFactura(String numeroFactura);

    // Búsquedas combinadas
    @Query("SELECT e FROM Entrada e WHERE e.proveedor.idProveedor = :idProveedor AND e.numeroFactura LIKE %:numeroFactura%")
    List<Entrada> findByProveedorIdAndNumeroFacturaContaining(Long idProveedor, String numeroFactura);

    @Query("SELECT e FROM Entrada e WHERE e.proveedor.idProveedor = :idProveedor AND e.numeroFactura LIKE %:numeroFactura% AND e.fechaEntrada BETWEEN :fechaInicio AND :fechaFin")
    List<Entrada> findByProveedorIdAndNumeroFacturaContainingAndFechaEntradaBetween(Long idProveedor, String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT e FROM Entrada e WHERE e.numeroFactura LIKE %:numeroFactura% AND e.fechaEntrada BETWEEN :fechaInicio AND :fechaFin")
    List<Entrada> findByNumeroFacturaContainingAndFechaEntradaBetween(String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin);
}

