package tienda.inventario.servicios;

import tienda.inventario.modelo.Cliente;
import java.util.List;
import java.util.Optional;

public interface IClienteServicio {
    List<Cliente> listarClientes();
    List<Cliente> listarClientesActivos();
    Optional<Cliente> obtenerClientePorId(Long id);
    Optional<Cliente> obtenerClientePorDni(String dni);
    Cliente guardarCliente(Cliente cliente);
    Cliente actualizarCliente(Long id, Cliente cliente);
    void eliminarCliente(Long id);
    List<Cliente> buscarClientes(String termino);
}
