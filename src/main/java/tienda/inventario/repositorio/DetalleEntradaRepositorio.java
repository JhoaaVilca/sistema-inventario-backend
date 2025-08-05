package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.DetalleEntrada;

public interface DetalleEntradaRepositorio extends JpaRepository<DetalleEntrada, Long> {
}
