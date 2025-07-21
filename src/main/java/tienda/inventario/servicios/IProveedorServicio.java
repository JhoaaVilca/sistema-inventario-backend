package tienda.inventario.servicios;

import tienda.inventario.modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface IProveedorServicio {

    List<Proveedor> listarProveedores();

    Proveedor guardarProveedor(Proveedor proveedor);

    void eliminarProveedor(Long id);

    Proveedor actualizarProveedor(Long id, Proveedor proveedor);

    Optional<Proveedor> buscarPorDocumento(String numeroDocumento);
}
