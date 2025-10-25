package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tienda.inventario.modelo.CajaDiaria;
import tienda.inventario.modelo.MovimientoCaja;
import tienda.inventario.repositorio.CajaDiariaRepositorio;
import tienda.inventario.repositorio.MovimientoCajaRepositorio;
import tienda.inventario.servicios.PdfServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CajaDiariaServicio implements ICajaDiariaServicio {

    private static final Logger logger = LoggerFactory.getLogger(CajaDiariaServicio.class);

    @Autowired
    private CajaDiariaRepositorio cajaRepositorio;

    @Autowired
    private MovimientoCajaRepositorio movimientoRepositorio;

    @Autowired
    private PdfServicio pdfServicio;

    @Override
    public Page<CajaDiaria> listarCajas(Pageable pageable) {
        return cajaRepositorio.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCaja> obtenerMovimientosPorCaja(Long idCaja) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
            .orElseThrow(() -> new RuntimeException("Caja no encontrada con ID: " + idCaja));
        return movimientoRepositorio.findByCajaDiariaOrderByFechaMovimientoDesc(caja);
    }

    @Override
    public CajaDiaria abrirCaja(BigDecimal montoApertura, String usuario, String observaciones) {
        LocalDate fechaActual = LocalDate.now();
        
        // Verificar si ya existe una caja abierta
        if (existeCajaAbierta()) {
            throw new RuntimeException("Ya existe una caja abierta. Debe cerrar la caja actual antes de abrir una nueva.");
        }

        // Verificar si ya existe una caja para el día actual
        if (cajaRepositorio.existsCajaAbiertaByFecha(fechaActual)) {
            throw new RuntimeException("Ya existe una caja para el día de hoy.");
        }

        CajaDiaria caja = new CajaDiaria();
        caja.setFecha(fechaActual);
        caja.setMontoApertura(montoApertura);
        caja.setTotalIngresos(BigDecimal.ZERO);
        caja.setTotalEgresos(BigDecimal.ZERO);
        caja.setEstado(CajaDiaria.EstadoCaja.ABIERTA);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setUsuarioApertura(usuario);
        caja.setObservaciones(observaciones);

        return cajaRepositorio.save(caja);
    }

    @Override
    public CajaDiaria cerrarCaja(Long idCaja, String usuario, String observaciones) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (caja.isCerrada()) {
            throw new RuntimeException("La caja ya está cerrada");
        }

        // Calcular totales antes de cerrar
        calcularTotalesCaja(idCaja);

        // Actualizar datos de cierre
        caja.setEstado(CajaDiaria.EstadoCaja.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setUsuarioCierre(usuario);
        caja.setMontoCierre(caja.getSaldoActual());
        
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            String obsActual = caja.getObservaciones();
            caja.setObservaciones(obsActual != null ? obsActual + "\n" + observaciones : observaciones);
        }

        return cajaRepositorio.save(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CajaDiaria> obtenerCajaAbierta() {
        Optional<CajaDiaria> cajaOpt = cajaRepositorio.findCajaAbiertaActual();
        if (cajaOpt.isPresent()) {
            CajaDiaria caja = cajaOpt.get();
            // Recalcular totales por seguridad
            calcularTotalesCaja(caja.getIdCaja());
            // Obtener la caja actualizada
            return cajaRepositorio.findById(caja.getIdCaja());
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CajaDiaria> obtenerCajaPorFecha(LocalDate fecha) {
        return cajaRepositorio.findByFecha(fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCajaAbierta() {
        return cajaRepositorio.existsCajaAbierta();
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiaria obtenerResumenCaja(Long idCaja) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        
        // Recalcular totales
        calcularTotalesCaja(idCaja);
        
        return caja;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaDiaria> listarCajasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        return cajaRepositorio.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaDiaria> obtenerHistorialCajas(int dias) {
        LocalDate fechaInicio = LocalDate.now().minusDays(dias);
        return cajaRepositorio.findByFechaBetween(fechaInicio, LocalDate.now());
    }

    @Override
    public MovimientoCaja registrarMovimiento(Long idCaja, MovimientoCaja.TipoMovimiento tipo, 
                                             BigDecimal monto, String descripcion, String usuario, 
                                             String referenciaDocumento) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (caja.isCerrada()) {
            throw new RuntimeException("No se pueden registrar movimientos en una caja cerrada");
        }

        logger.info("Registrando movimiento de caja. idCaja={}, tipo={}, monto={}, desc='{}', ref='{}', usuario={}",
                idCaja, tipo, monto, descripcion, referenciaDocumento, usuario);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setCajaDiaria(caja);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setUsuarioRegistro(usuario);
        movimiento.setReferenciaDocumento(referenciaDocumento);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        MovimientoCaja movimientoGuardado = movimientoRepositorio.save(movimiento);
        logger.info("Movimiento de caja guardado. idMovimiento={}, idCaja={}, tipo={}, monto={}",
                movimientoGuardado.getIdMovimiento(), idCaja, tipo, monto);
        
        // Recalcular totales de la caja
        calcularTotalesCaja(idCaja);

        return movimientoGuardado;
    }

    @Override
    public MovimientoCaja registrarIngresoVenta(Long idCaja, BigDecimal monto, String descripcion, 
                                              String usuario, Long idSalida) {
        MovimientoCaja movimiento = registrarMovimiento(idCaja, MovimientoCaja.TipoMovimiento.INGRESO, 
                                                       monto, descripcion, usuario, "VENTA-" + idSalida);
        
        // Aquí podrías establecer la relación con la salida si es necesario
        // movimiento.setSalida(salida);
        
        return movimiento;
    }

    @Override
    public MovimientoCaja registrarEgresoCompra(Long idCaja, BigDecimal monto, String descripcion, 
                                               String usuario, Long idEntrada) {
        MovimientoCaja movimiento = registrarMovimiento(idCaja, MovimientoCaja.TipoMovimiento.EGRESO, 
                                                       monto, descripcion, usuario, "COMPRA-" + idEntrada);
        
        // Aquí podrías establecer la relación con la entrada si es necesario
        // movimiento.setEntrada(entrada);
        
        return movimiento;
    }

    @Override
    public MovimientoCaja registrarIngresoPagoCredito(Long idCaja, BigDecimal monto, String descripcion, 
                                                     String usuario, Long idPagoCredito) {
        MovimientoCaja movimiento = registrarMovimiento(idCaja, MovimientoCaja.TipoMovimiento.INGRESO, 
                                                       monto, descripcion, usuario, "PAGO-CREDITO-" + idPagoCredito);
        
        // Aquí podrías establecer la relación con el pago de crédito si es necesario
        // movimiento.setPagoCredito(pagoCredito);
        
        return movimiento;
    }

    @Override
    public MovimientoCaja registrarEgresoManual(Long idCaja, BigDecimal monto, String descripcion, 
                                                String usuario, String observaciones) {
        MovimientoCaja movimiento = registrarMovimiento(idCaja, MovimientoCaja.TipoMovimiento.EGRESO, 
                                                       monto, descripcion, usuario, "GASTO-MANUAL");
        movimiento.setObservaciones(observaciones);
        
        return movimientoRepositorio.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCaja> obtenerMovimientosCaja(Long idCaja) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        
        return movimientoRepositorio.findByCajaDiariaOrderByFechaMovimientoDesc(caja);
    }

    @Override
    public void calcularTotalesCaja(Long idCaja) {
        CajaDiaria caja = cajaRepositorio.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        // Calcular totales desde la base de datos
        Double totalIngresos = movimientoRepositorio.calcularTotalIngresosPorCaja(caja);
        Double totalEgresos = movimientoRepositorio.calcularTotalEgresosPorCaja(caja);

        caja.setTotalIngresos(BigDecimal.valueOf(totalIngresos != null ? totalIngresos : 0.0));
        caja.setTotalEgresos(BigDecimal.valueOf(totalEgresos != null ? totalEgresos : 0.0));

        cajaRepositorio.save(caja);
    }

    @Override
    public byte[] generarReporteCaja(Long idCaja) {
        try {
            CajaDiaria caja = cajaRepositorio.findById(idCaja)
                    .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
            
            List<MovimientoCaja> movimientos = movimientoRepositorio.findByCajaDiariaOrderByFechaMovimientoDesc(caja);
            
            // Usar el servicio de PDF para generar el reporte
            return pdfServicio.generarReporteCaja(caja, movimientos);
        } catch (Exception e) {
            logger.error("Error al generar reporte de caja: ", e);
            throw new RuntimeException("Error al generar reporte de caja", e);
        }
    }
}
