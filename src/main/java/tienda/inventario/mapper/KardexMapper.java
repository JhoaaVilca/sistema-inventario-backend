package tienda.inventario.mapper;

import tienda.inventario.dto.KardexResponseDTO;
import tienda.inventario.modelo.Kardex;

public class KardexMapper {

    public static KardexResponseDTO toResponse(Kardex kardex) {
        KardexResponseDTO dto = new KardexResponseDTO();
        dto.setIdKardex(kardex.getIdKardex());
        dto.setIdProducto(kardex.getProducto().getIdProducto());
        dto.setNombreProducto(kardex.getProducto().getNombreProducto());
        dto.setFechaMovimiento(kardex.getFechaMovimiento());
        dto.setTipoMovimiento(kardex.getTipoMovimiento());
        dto.setCantidad(kardex.getCantidad());
        dto.setPrecioUnitario(kardex.getPrecioUnitario());
        dto.setValorTotal(kardex.getValorTotal());
        dto.setStockAnterior(kardex.getStockAnterior());
        dto.setStockActual(kardex.getStockActual());
        dto.setCostoPromedioAnterior(kardex.getCostoPromedioAnterior());
        dto.setCostoPromedioActual(kardex.getCostoPromedioActual());
        dto.setObservaciones(kardex.getObservaciones());
        dto.setReferenciaDocumento(kardex.getReferenciaDocumento());
        dto.setUsuarioRegistro(kardex.getUsuarioRegistro());
        return dto;
    }
}

