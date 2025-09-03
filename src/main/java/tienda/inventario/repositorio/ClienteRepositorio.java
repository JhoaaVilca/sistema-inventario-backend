package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.Cliente;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByDni(String dni);
    
        @Query("SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.idCliente DESC")
    List<Cliente> findClientesActivos();

    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND (c.nombres LIKE %:termino% OR c.apellidos LIKE %:termino% OR c.dni LIKE %:termino%) ORDER BY c.idCliente DESC")
    List<Cliente> buscarClientes(@Param("termino") String termino);
    
    boolean existsByDni(String dni);
    
    // Método personalizado para listar todos los clientes ordenados por ID descendente
    List<Cliente> findAllByOrderByIdClienteDesc();
}
