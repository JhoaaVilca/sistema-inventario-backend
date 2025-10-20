package tienda.inventario.dto;

import lombok.Data;

@Data
public class EmpresaResponseDTO {
    
    private Long idEmpresa;
    private String nombreEmpresa;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
    private String descripcion;
}



