package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.Entrada;

public interface EntradaRepositorio extends JpaRepository<Entrada, Long> {
}
