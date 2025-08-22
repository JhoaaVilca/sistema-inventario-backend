package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.DetalleSalida;

public interface DetalleSalidaRepositorio extends JpaRepository<DetalleSalida, Long> {
}


