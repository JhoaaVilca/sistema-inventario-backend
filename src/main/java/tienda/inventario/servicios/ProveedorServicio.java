package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tienda.inventario.modelo.Proveedor;
import tienda.inventario.repositorio.ProveedorRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
    public Page<Proveedor> listarProveedores(Pageable pageable) {
        return proveedorRepositorio.findAll(pageable);
    }

    @Override
    public Page<Proveedor> listarProveedoresActivos(Pageable pageable) {
        // ✅ solo activos
        return proveedorRepositorio.findByActivoTrue(pageable);
    }

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        validarDocumento(proveedor);
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
        validarDocumento(proveedor);
        proveedor.setIdProveedor(id);
        return proveedorRepositorio.save(proveedor);
    }

    @Override
    public Optional<Proveedor> buscarPorDocumento(String numeroDocumento) {
        return proveedorRepositorio.findByNumeroDocumento(numeroDocumento);
    }

    private void validarDocumento(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("Datos de proveedor inválidos");
        }
        String tipo = proveedor.getTipoDocumento() != null ? proveedor.getTipoDocumento().trim().toUpperCase() : "";
        String numero = proveedor.getNumeroDocumento() != null ? proveedor.getNumeroDocumento().trim() : "";
        if (tipo.isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (numero.isEmpty()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }
        if ("RUC".equals(tipo)) {
            if (!Pattern.matches("\\d{11}", numero)) {
                throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos numéricos");
            }
        } else if ("DNI".equals(tipo)) {
            if (!Pattern.matches("\\d{8}", numero)) {
                throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos");
            }
        } else {
            // Si manejas más tipos, agrega aquí. Por ahora, solo RUC o DNI.
            throw new IllegalArgumentException("Tipo de documento inválido: " + tipo);
        }
        // Normalizar el número al valor sanitizado
        proveedor.setNumeroDocumento(numero);
        proveedor.setTipoDocumento(tipo);
    }
}
