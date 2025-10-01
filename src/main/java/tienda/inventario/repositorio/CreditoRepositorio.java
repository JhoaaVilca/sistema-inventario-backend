package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.Credito;

@Repository
public interface CreditoRepositorio extends JpaRepository<Credito, Long> {
    Credito findBySalidaIdSalida(Long idSalida);
}


