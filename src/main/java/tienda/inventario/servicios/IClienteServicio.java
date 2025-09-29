package tienda.inventario.servicios;

import tienda.inventario.modelo.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IClienteServicio {
    List<Cliente> listarClientes();
    Page<Cliente> listarClientes(Pageable pageable);
    Page<Cliente> listarClientesActivos(Pageable pageable);
    Optional<Cliente> obtenerClientePorId(Long id);
    Optional<Cliente> obtenerClientePorDni(String dni);
    Cliente guardarCliente(Cliente cliente);
    Cliente actualizarCliente(Long id, Cliente cliente);
    void eliminarCliente(Long id);
    List<Cliente> buscarClientes(String termino);
}
