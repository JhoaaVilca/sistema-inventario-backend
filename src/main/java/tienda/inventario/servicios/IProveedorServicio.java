package tienda.inventario.servicios;

import tienda.inventario.modelo.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IProveedorServicio {
    List<Proveedor> listarProveedores();          // todos
    Page<Proveedor> listarProveedores(Pageable pageable);
    Page<Proveedor> listarProveedoresActivos(Pageable pageable);   // solo activos
    Proveedor guardarProveedor(Proveedor proveedor);
    void desactivarProveedor(Long id);
    void activarProveedor(Long id);
    Proveedor actualizarProveedor(Long id, Proveedor proveedor);
    Optional<Proveedor> buscarPorDocumento(String numeroDocumento);
}
