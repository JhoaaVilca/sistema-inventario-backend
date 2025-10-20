package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.modelo.Empresa;
import tienda.inventario.repositorio.EmpresaRepositorio;

@Service
public class EmpresaServicio implements IEmpresaServicio {

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    @Override
    public Empresa obtenerConfiguracion() {
        Empresa empresa = empresaRepositorio.findFirstByOrderByIdEmpresa();
        
        // Si no existe configuración, crear una por defecto
        if (empresa == null) {
            empresa = new Empresa("Comercial Yoli", "12345678901", "Av. Principal 123, Lima");
            empresa = empresaRepositorio.save(empresa);
        }
        
        return empresa;
    }

    @Override
    @Transactional
    public Empresa actualizarConfiguracion(Empresa empresa) {
        // Buscar la configuración existente
        Empresa empresaExistente = empresaRepositorio.findFirstByOrderByIdEmpresa();
        
        if (empresaExistente != null) {
            // Actualizar la configuración existente
            empresaExistente.setNombreEmpresa(empresa.getNombreEmpresa());
            empresaExistente.setRuc(empresa.getRuc());
            empresaExistente.setDireccion(empresa.getDireccion());
            empresaExistente.setTelefono(empresa.getTelefono());
            empresaExistente.setEmail(empresa.getEmail());
            empresaExistente.setDescripcion(empresa.getDescripcion());
            return empresaRepositorio.save(empresaExistente);
        } else {
            // Crear nueva configuración
            return empresaRepositorio.save(empresa);
        }
    }
}



