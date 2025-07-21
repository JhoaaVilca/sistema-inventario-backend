package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.modelo.Proveedor;
import tienda.inventario.repositorio.ProveedorRepositorio;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorServicio implements IProveedorServicio {

    @Autowired
    private ProveedorRepositorio proveedorRepositorio;

    @Override
    public List<Proveedor> listarProveedores() {
        return proveedorRepositorio.findAll();
    }

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        return proveedorRepositorio.save(proveedor);
    }

    @Override
    public void eliminarProveedor(Long id) {
        proveedorRepositorio.deleteById(id);
    }

    @Override
    public Proveedor actualizarProveedor(Long id, Proveedor proveedor) {
        proveedor.setIdProveedor(id);
        return proveedorRepositorio.save(proveedor);
    }

    @Override
    public Optional<Proveedor> buscarPorDocumento(String numeroDocumento) {
        return proveedorRepositorio.findByNumeroDocumento(numeroDocumento);
    }
}
