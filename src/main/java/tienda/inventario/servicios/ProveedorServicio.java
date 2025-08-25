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
        // 🔄 todos (activos e inactivos)
        return proveedorRepositorio.findAll();
    }

    @Override
    public List<Proveedor> listarProveedoresActivos() {
        // ✅ solo activos
        return proveedorRepositorio.findByActivoTrue();
    }

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        proveedor.setActivo(true);
        return proveedorRepositorio.save(proveedor);
    }

    @Override
    public void desactivarProveedor(Long id) {
        Proveedor proveedor = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setActivo(false);
        proveedorRepositorio.save(proveedor);
    }

    @Override
    public void activarProveedor(Long id) {
        Proveedor proveedor = proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setActivo(true);
        proveedorRepositorio.save(proveedor);
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
