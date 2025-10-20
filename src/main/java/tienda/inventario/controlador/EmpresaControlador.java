package tienda.inventario.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tienda.inventario.dto.EmpresaRequestDTO;
import tienda.inventario.dto.EmpresaResponseDTO;
import tienda.inventario.modelo.Empresa;
import tienda.inventario.servicios.IEmpresaServicio;

@RestController
@RequestMapping("/api/empresa")
@CrossOrigin(origins = "http://localhost:3001")
public class EmpresaControlador {

    @Autowired
    private IEmpresaServicio empresaServicio;

    @GetMapping
    public ResponseEntity<EmpresaResponseDTO> obtenerConfiguracion() {
        Empresa empresa = empresaServicio.obtenerConfiguracion();
        EmpresaResponseDTO response = new EmpresaResponseDTO();
        
        response.setIdEmpresa(empresa.getIdEmpresa());
        response.setNombreEmpresa(empresa.getNombreEmpresa());
        response.setRuc(empresa.getRuc());
        response.setDireccion(empresa.getDireccion());
        response.setTelefono(empresa.getTelefono());
        response.setEmail(empresa.getEmail());
        response.setDescripcion(empresa.getDescripcion());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<EmpresaResponseDTO> actualizarConfiguracion(@RequestBody EmpresaRequestDTO request) {
        Empresa empresa = new Empresa();
        empresa.setNombreEmpresa(request.getNombreEmpresa());
        empresa.setRuc(request.getRuc());
        empresa.setDireccion(request.getDireccion());
        empresa.setTelefono(request.getTelefono());
        empresa.setEmail(request.getEmail());
        empresa.setDescripcion(request.getDescripcion());
        
        Empresa empresaActualizada = empresaServicio.actualizarConfiguracion(empresa);
        
        EmpresaResponseDTO response = new EmpresaResponseDTO();
        response.setIdEmpresa(empresaActualizada.getIdEmpresa());
        response.setNombreEmpresa(empresaActualizada.getNombreEmpresa());
        response.setRuc(empresaActualizada.getRuc());
        response.setDireccion(empresaActualizada.getDireccion());
        response.setTelefono(empresaActualizada.getTelefono());
        response.setEmail(empresaActualizada.getEmail());
        response.setDescripcion(empresaActualizada.getDescripcion());
        
        return ResponseEntity.ok(response);
    }
}



