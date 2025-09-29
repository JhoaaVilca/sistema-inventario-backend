package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tienda.inventario.modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrue(); // solo activos
    Page<Proveedor> findByActivoTrue(Pageable pageable); // solo activos paginado
    Optional<Proveedor> findByNumeroDocumento(String numeroDocumento);
}