package tienda.inventario.servicios;

import org.springframework.stereotype.Service;
import tienda.inventario.modelo.Cliente;
import tienda.inventario.repositorio.ClienteRepositorio;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServicio implements IClienteServicio {

    private final ClienteRepositorio repositorio;

    public ClienteServicio(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Cliente> listarClientes() {
        return repositorio.findAllByOrderByIdClienteDesc();
    }

    @Override
    public List<Cliente> listarClientesActivos() {
        return repositorio.findClientesActivos();
    }

    @Override
    public Optional<Cliente> obtenerClientePorId(Long id) {
        return repositorio.findById(id);
    }

    @Override
    public Optional<Cliente> obtenerClientePorDni(String dni) {
        return repositorio.findByDni(dni);
    }

    @Override
    public Cliente guardarCliente(Cliente cliente) {
        // Validar que el DNI no exista
        if (repositorio.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException("Ya existe un cliente con el DNI: " + cliente.getDni());
        }
        return repositorio.save(cliente);
    }

    @Override
    public Cliente actualizarCliente(Long id, Cliente cliente) {
        Cliente clienteExistente = repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        
        // Si cambia el DNI, validar que no exista en otro cliente
        if (!clienteExistente.getDni().equals(cliente.getDni()) && 
            repositorio.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException("Ya existe otro cliente con el DNI: " + cliente.getDni());
        }
        
        clienteExistente.setDni(cliente.getDni());
        clienteExistente.setNombres(cliente.getNombres());
        clienteExistente.setApellidos(cliente.getApellidos());
        clienteExistente.setDireccion(cliente.getDireccion());
        clienteExistente.setTelefono(cliente.getTelefono());
        clienteExistente.setEmail(cliente.getEmail());
        clienteExistente.setActivo(cliente.getActivo());
        
        return repositorio.save(clienteExistente);
    }

    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        
        // Lógica de eliminación lógica
        cliente.setActivo(false);
        repositorio.save(cliente);
    }

    @Override
    public List<Cliente> buscarClientes(String termino) {
        return repositorio.buscarClientes(termino);
    }
}
