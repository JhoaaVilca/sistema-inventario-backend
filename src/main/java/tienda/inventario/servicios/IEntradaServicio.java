package tienda.inventario.servicios;

import tienda.inventario.modelo.Entrada;
import java.time.LocalDate;
import java.util.List;

public interface IEntradaServicio {
    Entrada guardarEntrada(Entrada entrada);
    List<Entrada> listarEntradas();
    Entrada actualizarEntrada(Long id, Entrada entrada);
    void eliminarEntrada(Long id);

    // Métodos de filtrado
    List<Entrada> filtrarPorProveedor(Long idProveedor);
    List<Entrada> filtrarPorFecha(LocalDate fecha);
    List<Entrada> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
    List<Entrada> filtrarPorProveedorYRangoFechas(Long idProveedor, LocalDate fechaInicio, LocalDate fechaFin);
}
