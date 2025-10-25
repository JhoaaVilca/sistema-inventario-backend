package tienda.inventario.servicios;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tienda.inventario.modelo.Credito;
import tienda.inventario.modelo.PagoCredito;

public interface ICreditoServicio {
    Credito crearCredito(Credito credito);
    Page<Credito> listarCreditos(Pageable pageable);
    Credito obtenerPorId(Long id);
    PagoCredito registrarPago(Long idCredito, PagoCredito pago, Long idCaja, String usuario);
}


