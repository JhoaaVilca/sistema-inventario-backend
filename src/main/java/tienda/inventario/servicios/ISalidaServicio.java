package tienda.inventario.servicios;

import tienda.inventario.modelo.Salida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface ISalidaServicio {
    Salida guardarSalida(Salida salida);
    List<Salida> listarSalidas();
    Page<Salida> listarSalidas(Pageable pageable);
    Salida actualizarSalida(Long id, Salida salida);
    void eliminarSalida(Long id);

    List<Salida> filtrarPorFecha(LocalDate fecha);
    List<Salida> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
}


