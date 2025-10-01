package tienda.inventario.mapper;

import tienda.inventario.dto.*;
import tienda.inventario.modelo.*;
import java.util.stream.Collectors;

public class CreditoMapper {
    public static Credito toEntity(CreditoRequestDTO dto, Salida salida, Cliente cliente) {
        Credito c = new Credito();
        c.setSalida(salida);
        c.setCliente(cliente);
        c.setMontoTotal(dto.getMontoTotal());
        c.setSaldoPendiente(dto.getSaldoPendiente() != null ? dto.getSaldoPendiente() : dto.getMontoTotal());
        c.setFechaInicio(salida != null ? salida.getFechaSalida() : null);
        c.setFechaVencimiento(dto.getFechaVencimiento());
        c.setEstado("PENDIENTE");
        c.setObservacion(dto.getObservacion());
        return c;
    }

    public static CreditoResponseDTO toResponse(Credito c) {
        CreditoResponseDTO dto = new CreditoResponseDTO();
        dto.setIdCredito(c.getIdCredito());
        dto.setIdSalida(c.getSalida() != null ? c.getSalida().getIdSalida() : null);
        dto.setIdCliente(c.getCliente() != null ? c.getCliente().getIdCliente() : null);
        dto.setNombreCliente(c.getCliente() != null ? c.getCliente().getNombres() : null);
        dto.setMontoTotal(c.getMontoTotal());
        dto.setSaldoPendiente(c.getSaldoPendiente());
        dto.setFechaInicio(c.getFechaInicio());
        dto.setFechaVencimiento(c.getFechaVencimiento());
        dto.setEstado(c.getEstado());
        dto.setObservacion(c.getObservacion());
        dto.setPagos(c.getPagos() == null ? null : c.getPagos().stream().map(p -> {
            PagoCreditoResponseDTO pr = new PagoCreditoResponseDTO();
            pr.setIdPago(p.getIdPago());
            pr.setFechaPago(p.getFechaPago());
            pr.setMonto(p.getMonto());
            pr.setMedioPago(p.getMedioPago());
            pr.setObservacion(p.getObservacion());
            return pr;
        }).collect(Collectors.toList()));
        return dto;
    }
}


