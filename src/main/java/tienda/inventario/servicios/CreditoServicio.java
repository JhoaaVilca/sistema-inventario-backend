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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CreditoServicio implements ICreditoServicio {

    private static final Logger logger = LoggerFactory.getLogger(CreditoServicio.class);

    @Autowired
    private CreditoRepositorio creditoRepositorio;

    @Autowired
    private PagoCreditoRepositorio pagoCreditoRepositorio;
    
    @Autowired
    private CajaDiariaServicio cajaDiariaServicio;

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
    public PagoCredito registrarPago(Long idCredito, PagoCredito pago, Long idCaja, String usuario) {
        // Validar pago
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new RuntimeException("Monto de pago inválido");
        }
        
        // Obtener y validar crédito
        Credito credito = obtenerPorId(idCredito);
        double nuevoSaldo = (credito.getSaldoPendiente() == null ? 0.0 : credito.getSaldoPendiente()) - pago.getMonto();
        if (nuevoSaldo < 0) {
            throw new RuntimeException("El pago excede el saldo pendiente");
        }
        
        // Registrar el pago
        pago.setCredito(credito);
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDate.now());
        }
        PagoCredito guardado = pagoCreditoRepositorio.save(pago);

        // Actualizar estado del crédito
        credito.setSaldoPendiente(nuevoSaldo);
        if (nuevoSaldo == 0) {
            credito.setEstado("CANCELADO");
        } else if (credito.getFechaVencimiento() != null && credito.getFechaVencimiento().isBefore(LocalDate.now())) {
            credito.setEstado("VENCIDO");
        } else {
            credito.setEstado("PARCIAL");
        }
        creditoRepositorio.save(credito);
        
        // Resolver caja destino: usar la proporcionada o, si es nula, la caja abierta actual
        Long cajaDestinoId = idCaja;
        if (cajaDestinoId == null) {
            try {
                cajaDestinoId = cajaDiariaServicio.obtenerCajaAbierta()
                        .map(c -> c.getIdCaja())
                        .orElse(null);
                if (cajaDestinoId != null) {
                    logger.info("No se envió idCaja desde frontend. Usando caja abierta actual idCaja={}", cajaDestinoId);
                }
            } catch (Exception ex) {
                logger.warn("No se pudo obtener caja abierta automáticamente: {}", ex.getMessage());
            }
        }

        // Registrar el ingreso en caja si se pudo determinar una caja
        if (cajaDestinoId != null) {
            try {
                logger.info("Registrando movimiento en caja por pago de crédito. idCaja={}, idCredito={}, idPago={}, monto={}",
                        cajaDestinoId, credito.getIdCredito(), guardado.getIdPago(), pago.getMonto());
                cajaDiariaServicio.registrarIngresoPagoCredito(
                    cajaDestinoId,
                    BigDecimal.valueOf(pago.getMonto()),
                    "Pago de crédito #" + credito.getIdCredito() + " / Pago #" + guardado.getIdPago(),
                    usuario,
                    guardado.getIdPago()
                );
            } catch (Exception e) {
                // No hacemos rollback del pago si falla el registro en caja, solo registramos el error para seguimiento
                logger.error("Error al registrar pago en caja (idCaja={}, idPago={}): {}", cajaDestinoId, guardado.getIdPago(), e.getMessage(), e);
            }
        }
        
        return guardado;
    }
}


