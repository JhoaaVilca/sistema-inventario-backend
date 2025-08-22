package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.Salida;

import java.time.LocalDate;
import java.util.List;

public interface SalidaRepositorio extends JpaRepository<Salida, Long> {
    List<Salida> findByFechaSalida(LocalDate fechaSalida);
    List<Salida> findByFechaSalidaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}


