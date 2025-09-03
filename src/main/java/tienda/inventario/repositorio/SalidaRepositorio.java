package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import tienda.inventario.modelo.Salida;

import java.time.LocalDate;
import java.util.List;

public interface SalidaRepositorio extends JpaRepository<Salida, Long> {
    List<Salida> findByFechaSalidaOrderByIdSalidaDesc(LocalDate fechaSalida);
    List<Salida> findByFechaSalidaBetweenOrderByIdSalidaDesc(LocalDate fechaInicio, LocalDate fechaFin);
    
    // Método personalizado para listar todas las salidas ordenadas por ID descendente
    List<Salida> findAllByOrderByIdSalidaDesc();
}


