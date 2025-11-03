package tienda.inventario.servicios;

import org.springframework.stereotype.Service;
import tienda.inventario.dto.*;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.modelo.PagoCredito;
import tienda.inventario.modelo.Salida;
import tienda.inventario.repositorio.*;
import tienda.inventario.dto.ProductoPorVencerDTO;
import tienda.inventario.dto.DeudorDTO;
import tienda.inventario.dto.VencimientosDTO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final EntradaRepositorio entradaRepositorio;
    private final SalidaRepositorio salidaRepositorio;
    private final CreditoRepositorio creditoRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final PagoCreditoRepositorio pagoCreditoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public DashboardServicio(ProductoRepositorio productoRepositorio,
                             ProveedorRepositorio proveedorRepositorio,
                             EntradaRepositorio entradaRepositorio,
                             SalidaRepositorio salidaRepositorio,
                             CreditoRepositorio creditoRepositorio,
                             LoteRepositorio loteRepositorio,
                             PagoCreditoRepositorio pagoCreditoRepositorio,
                             CategoriaRepositorio categoriaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.entradaRepositorio = entradaRepositorio;
        this.salidaRepositorio = salidaRepositorio;
        this.creditoRepositorio = creditoRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.pagoCreditoRepositorio = pagoCreditoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public DashboardResumenDTO obtenerResumen() {
        DashboardResumenDTO dto = new DashboardResumenDTO();
        dto.productosTotales = productoRepositorio.count();
        dto.proveedoresTotales = proveedorRepositorio.count();
        dto.entradasTotales = entradasDelMes();
        dto.ventasTotales = ventasDelMes();
        dto.ventasCantidadMes = conteoSalidasDelMes();
        dto.productosSinStock = productoRepositorio.countByStockEquals(0);
        dto.creditosPendientes = creditoRepositorio.countBySaldoPendienteGreaterThan(0.0);
        dto.productosPorVencer = loteRepositorio.findLotesProximosAVencer(LocalDate.now(), LocalDate.now().plusDays(30)).size();
        dto.categoriasTotales = categoriaRepositorio.count();

        dto.actividadReciente = actividadReciente(10);
        dto.resumenMensual = resumenMensual();
        dto.alertas = alertas();
        return dto;
    }

    private long entradasDelMes() {
        YearMonth ym = YearMonth.now();
        return entradaRepositorio.findByFechaEntradaBetween(ym.atDay(1), ym.atEndOfMonth()).size();
    }

    private double ventasDelMes() {
        YearMonth ym = YearMonth.now();
        return salidaRepositorio.findByFechaSalidaBetweenOrderByIdSalidaDesc(ym.atDay(1), ym.atEndOfMonth()).stream()
                .mapToDouble(s -> Optional.ofNullable(s.getTotalSalida()).orElse(0.0)).sum();
    }

    private long conteoSalidasDelMes() {
        YearMonth ym = YearMonth.now();
        return salidaRepositorio.findByFechaSalidaBetweenOrderByIdSalidaDesc(ym.atDay(1), ym.atEndOfMonth()).size();
    }

    public List<DashboardActividadDTO> actividadReciente(int limite) {
        List<DashboardActividadDTO> items = new ArrayList<>();

        // Últimas salidas
        salidaRepositorio.findAllByOrderByIdSalidaDesc().stream().limit(limite)
                .forEach(s -> items.add(mapSalida(s)));

        // Últimas entradas
        entradaRepositorio.findByFechaEntradaBetween(LocalDate.now().minusDays(90), LocalDate.now()).stream()
                .sorted(Comparator.comparing(Entrada::getIdEntrada).reversed())
                .limit(limite)
                .forEach(e -> items.add(mapEntrada(e)));

        // Últimos pagos de crédito
        pagoCreditoRepositorio.findAll().stream()
                .sorted(Comparator.comparing(PagoCredito::getIdPago).reversed())
                .limit(limite)
                .forEach(p -> items.add(mapPago(p)));

        // Ordenar por fecha desc y recortar a limite
        return items.stream()
                .sorted(Comparator.comparing((DashboardActividadDTO a) -> a.fecha).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    private DashboardActividadDTO mapSalida(Salida s) {
        DashboardActividadDTO a = new DashboardActividadDTO();
        a.fecha = s.getFechaSalida();
        a.tipo = "Venta";
        a.monto = s.getTotalSalida();
        String cliente = "Consumidor final";
        if (s.getCliente() != null) {
            String nombres = Optional.ofNullable(s.getCliente().getNombres()).orElse("");
            String apellidos = Optional.ofNullable(s.getCliente().getApellidos()).orElse("");
            String full = (nombres + " " + apellidos).trim();
            if (!full.isEmpty()) cliente = full;
        }
        a.detalle = "Venta " + (s.getIdSalida() != null ? ("#" + s.getIdSalida()) : "") + " - " + cliente;
        a.usuario = "sistema"; // si tienes auditoría, reemplazar
        return a;
    }

    private DashboardActividadDTO mapEntrada(Entrada e) {
        DashboardActividadDTO a = new DashboardActividadDTO();
        a.fecha = e.getFechaEntrada();
        a.tipo = "Entrada";
        a.monto = e.getTotalEntrada();
        String proveedor = e.getProveedor() != null ? e.getProveedor().getNombre() : "Proveedor";
        a.detalle = "Entrada " + (e.getNumeroFactura() != null ? e.getNumeroFactura() : "#" + e.getIdEntrada()) + " - " + proveedor;
        a.usuario = "sistema";
        return a;
    }

    private DashboardActividadDTO mapPago(PagoCredito p) {
        DashboardActividadDTO a = new DashboardActividadDTO();
        a.fecha = p.getFechaPago();
        a.tipo = "Pago Crédito";
        a.monto = p.getMonto();
        String cliente = "Cliente";
        if (p.getCredito() != null && p.getCredito().getCliente() != null) {
            String nombres = Optional.ofNullable(p.getCredito().getCliente().getNombres()).orElse("");
            String apellidos = Optional.ofNullable(p.getCredito().getCliente().getApellidos()).orElse("");
            String full = (nombres + " " + apellidos).trim();
            if (!full.isEmpty()) cliente = full;
        }
        a.detalle = cliente + " (" + p.getMonto() + ")";
        a.usuario = "sistema";
        return a;
    }

    public List<DashboardMensualDTO> resumenMensual() {
        List<DashboardMensualDTO> lista = new ArrayList<>();
        YearMonth ahora = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = ahora.minusMonths(i);
            LocalDate ini = ym.atDay(1);
            LocalDate fin = ym.atEndOfMonth();

            double ventas = salidaRepositorio.findByFechaSalidaBetweenOrderByIdSalidaDesc(ini, fin).stream()
                    .mapToDouble(s -> Optional.ofNullable(s.getTotalSalida()).orElse(0.0)).sum();
            double compras = entradaRepositorio.findByFechaEntradaBetween(ini, fin).stream()
                    .mapToDouble(e -> Optional.ofNullable(e.getTotalEntrada()).orElse(0.0)).sum();

            DashboardMensualDTO d = new DashboardMensualDTO();
            d.mes = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "PE"));
            d.ventas = ventas;
            d.compras = compras;
            lista.add(d);
        }
        return lista;
    }

    public List<DashboardAlertaDTO> alertas() {
        List<DashboardAlertaDTO> out = new ArrayList<>();

        int sinStock = (int) productoRepositorio.countByStockEquals(0);
        if (sinStock > 0) {
            DashboardAlertaDTO a = new DashboardAlertaDTO();
            a.tipo = "SIN_STOCK";
            a.cantidad = sinStock;
            a.mensaje = sinStock + " productos sin stock — revisar reposición.";
            out.add(a);
        }

        int vencidos = (int) creditoRepositorio.countByEstado("VENCIDO");
        if (vencidos > 0) {
            DashboardAlertaDTO a = new DashboardAlertaDTO();
            a.tipo = "CREDITOS_VENCIDOS";
            a.cantidad = vencidos;
            a.mensaje = vencidos + " créditos vencidos — contactar clientes.";
            out.add(a);
        }

        int porVencer = loteRepositorio.findLotesProximosAVencer(LocalDate.now(), LocalDate.now().plusDays(30)).size();
        if (porVencer > 0) {
            DashboardAlertaDTO a = new DashboardAlertaDTO();
            a.tipo = "POR_VENCER";
            a.cantidad = porVencer;
            a.mensaje = porVencer + " productos próximos a vencer.";
            out.add(a);
        }

        return out;
    }

    public List<ProductoPorVencerDTO> proximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);
        return loteRepositorio.findLotesProximosAVencer(hoy, limite).stream()
                .map(l -> {
                    ProductoPorVencerDTO d = new ProductoPorVencerDTO();
                    if (l.getDetalleEntrada() != null && l.getDetalleEntrada().getProducto() != null) {
                        d.idProducto = l.getDetalleEntrada().getProducto().getIdProducto();
                        d.nombreProducto = l.getDetalleEntrada().getProducto().getNombreProducto();
                    }
                    d.fechaVencimiento = l.getFechaVencimiento();
                    d.diasRestantes = d.fechaVencimiento != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, d.fechaVencimiento) : 0;
                    return d;
                })
                .collect(Collectors.toList());
    }

    public VencimientosDTO listarVencimientos() {
        LocalDate hoy = LocalDate.now();
        VencimientosDTO dto = new VencimientosDTO();
        dto.proximos = proximosAVencer();
        dto.vencidos = loteRepositorio.findLotesVencidos(hoy).stream()
                .map(l -> {
                    ProductoPorVencerDTO d = new ProductoPorVencerDTO();
                    if (l.getDetalleEntrada() != null && l.getDetalleEntrada().getProducto() != null) {
                        d.idProducto = l.getDetalleEntrada().getProducto().getIdProducto();
                        d.nombreProducto = l.getDetalleEntrada().getProducto().getNombreProducto();
                    }
                    d.fechaVencimiento = l.getFechaVencimiento();
                    d.diasRestantes = d.fechaVencimiento != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, d.fechaVencimiento) : 0;
                    return d;
                })
                .collect(Collectors.toList());
        return dto;
    }

    public List<DeudorDTO> deudoresTop(int limite) {
        Map<Long, DeudorDTO> mapa = new HashMap<>();
        creditoRepositorio.findAll().stream()
                .filter(c -> Optional.ofNullable(c.getSaldoPendiente()).orElse(0.0) > 0.0)
                .forEach(c -> {
                    if (c.getCliente() == null) return;
                    Long id = c.getCliente().getIdCliente();
                    DeudorDTO d = mapa.getOrDefault(id, new DeudorDTO());
                    d.idCliente = id;
                    String nombres = Optional.ofNullable(c.getCliente().getNombres()).orElse("");
                    String apellidos = Optional.ofNullable(c.getCliente().getApellidos()).orElse("");
                    d.nombreCompleto = (nombres + " " + apellidos).trim();
                    d.saldoPendiente = Optional.ofNullable(d.saldoPendiente).orElse(0.0) + Optional.ofNullable(c.getSaldoPendiente()).orElse(0.0);
                    mapa.put(id, d);
                });

        return mapa.values().stream()
                .sorted(Comparator.comparing((DeudorDTO d) -> d.saldoPendiente).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }
}


