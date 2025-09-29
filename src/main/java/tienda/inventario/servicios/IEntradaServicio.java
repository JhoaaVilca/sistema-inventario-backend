package tienda.inventario.servicios;

import tienda.inventario.modelo.Entrada;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

public interface IEntradaServicio {
    Entrada guardarEntrada(Entrada entrada);
    List<Entrada> listarEntradas();
    Page<Entrada> listarEntradas(Pageable pageable);
    Entrada actualizarEntrada(Long id, Entrada entrada);
    void eliminarEntrada(Long id);

    // Métodos de filtrado
    List<Entrada> filtrarPorProveedor(Long idProveedor);
    List<Entrada> filtrarPorFecha(LocalDate fecha);
    List<Entrada> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
    List<Entrada> filtrarPorProveedorYRangoFechas(Long idProveedor, LocalDate fechaInicio, LocalDate fechaFin);
    List<Entrada> filtrarPorNumeroFactura(String numeroFactura);
    
    // Métodos de filtrado combinado
    List<Entrada> filtrarPorProveedorYNumeroFactura(Long idProveedor, String numeroFactura);
    List<Entrada> filtrarPorProveedorYNumeroFacturaYRangoFechas(Long idProveedor, String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin);
    List<Entrada> filtrarPorNumeroFacturaYRangoFechas(String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin);

    // Métodos para facturas
    String subirFactura(Long idEntrada, MultipartFile file);
    Resource descargarFactura(Long idEntrada);
}
