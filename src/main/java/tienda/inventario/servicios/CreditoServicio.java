package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.modelo.Credito;
import tienda.inventario.modelo.PagoCredito;
import tienda.inventario.repositorio.CreditoRepositorio;
import tienda.inventario.repositorio.PagoCreditoRepositorio;

import java.time.LocalDate;

@Service
public class CreditoServicio implements ICreditoServicio {

    @Autowired
    private CreditoRepositorio creditoRepositorio;

    @Autowired
    private PagoCreditoRepositorio pagoCreditoRepositorio;

    @Override
    @Transactional
    public Credito crearCredito(Credito credito) {
        if (credito.getSaldoPendiente() == null) {
            credito.setSaldoPendiente(credito.getMontoTotal());
        }
        if (credito.getFechaInicio() == null) {
            credito.setFechaInicio(LocalDate.now());
        }
        if (credito.getEstado() == null) {
            credito.setEstado("PENDIENTE");
        }
        return creditoRepositorio.save(credito);
    }

    @Override
    public Page<Credito> listarCreditos(Pageable pageable) {
        return creditoRepositorio.findAll(pageable);
    }

    @Override
    public Credito obtenerPorId(Long id) {
        return creditoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
    }

    @Override
    @Transactional
    public PagoCredito registrarPago(Long idCredito, PagoCredito pago) {
        Credito credito = obtenerPorId(idCredito);
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new RuntimeException("Monto de pago inválido");
        }
        double nuevoSaldo = (credito.getSaldoPendiente() == null ? 0.0 : credito.getSaldoPendiente()) - pago.getMonto();
        if (nuevoSaldo < 0) {
            throw new RuntimeException("El pago excede el saldo pendiente");
        }
        pago.setCredito(credito);
        PagoCredito guardado = pagoCreditoRepositorio.save(pago);

        credito.setSaldoPendiente(nuevoSaldo);
        if (nuevoSaldo == 0) {
            credito.setEstado("CANCELADO");
        } else if (credito.getFechaVencimiento() != null && credito.getFechaVencimiento().isBefore(java.time.LocalDate.now())) {
            credito.setEstado("VENCIDO");
        } else {
            credito.setEstado("PARCIAL");
        }
        creditoRepositorio.save(credito);
        return guardado;
    }
}


