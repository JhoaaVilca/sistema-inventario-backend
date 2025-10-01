package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.PagoCredito;

@Repository
public interface PagoCreditoRepositorio extends JpaRepository<PagoCredito, Long> {
}


