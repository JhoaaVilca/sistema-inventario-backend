package tienda.inventario.servicios;

import tienda.inventario.modelo.Salida;
import java.time.LocalDate;
import java.util.List;

public interface ISalidaServicio {
    Salida guardarSalida(Salida salida);
    List<Salida> listarSalidas();
    Salida actualizarSalida(Long id, Salida salida);
    void eliminarSalida(Long id);

    List<Salida> filtrarPorFecha(LocalDate fecha);
    List<Salida> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
}


