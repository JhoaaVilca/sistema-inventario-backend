package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.Empresa;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
    
    // Método para obtener la primera (y única) configuración de empresa
    Empresa findFirstByOrderByIdEmpresa();
}



