package tienda.inventario.servicios;

import tienda.inventario.modelo.Proveedor;
import java.util.List;
import java.util.Optional;

public interface IProveedorServicio {
    List<Proveedor> listarProveedores();          // todos
    List<Proveedor> listarProveedoresActivos();   // solo activos
    Proveedor guardarProveedor(Proveedor proveedor);
    void desactivarProveedor(Long id);
    void activarProveedor(Long id);
    Proveedor actualizarProveedor(Long id, Proveedor proveedor);
    Optional<Proveedor> buscarPorDocumento(String numeroDocumento);
}
