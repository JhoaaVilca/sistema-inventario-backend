package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrue(); // solo activos
    Optional<Proveedor> findByNumeroDocumento(String numeroDocumento);
}