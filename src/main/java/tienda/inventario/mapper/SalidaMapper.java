package tienda.inventario.mapper;

import tienda.inventario.dto.*;
import tienda.inventario.modelo.*;

import java.util.List;
import java.util.stream.Collectors;

public class SalidaMapper {
	public static Salida toEntity(SalidaRequestDTO dto, List<Producto> productos) {
		Salida salida = new Salida();
		salida.setFechaSalida(dto.getFechaSalida());
		List<DetalleSalida> detalles = dto.getDetalles().stream().map(detDto -> {
			DetalleSalida det = new DetalleSalida();
			Producto producto = productos.stream()
					.filter(p -> p.getIdProducto().equals(detDto.getIdProducto()))
					.findFirst()
					.orElse(null);
			det.setProducto(producto);
			det.setCantidad(detDto.getCantidad());
			det.setPrecioUnitario(detDto.getPrecioUnitario());
			return det;
		}).collect(Collectors.toList());
		salida.setDetalles(detalles);
		return salida;
	}

	public static SalidaResponseDTO toResponse(Salida salida) {
		SalidaResponseDTO dto = new SalidaResponseDTO();
		dto.setIdSalida(salida.getIdSalida());
		dto.setFechaSalida(salida.getFechaSalida());
		dto.setTotalSalida(salida.getTotalSalida());
		dto.setDetalles(salida.getDetalles() == null ? null : salida.getDetalles().stream().map(det -> {
			DetalleSalidaResponseDTO d = new DetalleSalidaResponseDTO();
			d.setIdDetalle(det.getIdDetalle());
			if (det.getProducto() != null) {
				d.setIdProducto(det.getProducto().getIdProducto());
				d.setNombreProducto(det.getProducto().getNombreProducto());
			}
			d.setCantidad(det.getCantidad());
			d.setPrecioUnitario(det.getPrecioUnitario());
			d.setSubtotal(det.getSubtotal());
			return d;
		}).collect(Collectors.toList()));
		return dto;
	}
}
