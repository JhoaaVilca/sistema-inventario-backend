package tienda.inventario.mapper;

import tienda.inventario.dto.*;
import tienda.inventario.modelo.*;

import java.util.List;
import java.util.stream.Collectors;

public class SalidaMapper {
	public static Salida toEntity(SalidaRequestDTO dto, List<Producto> productos, Cliente cliente) {
		Salida salida = new Salida();
		salida.setFechaSalida(dto.getFechaSalida());
		salida.setCliente(cliente);
		salida.setTipoVenta(dto.getTipoVenta() != null ? dto.getTipoVenta() : "CONTADO");
		
		List<DetalleSalida> detalles = dto.getDetalles().stream().map(detDto -> {
			DetalleSalida det = new DetalleSalida();
			Producto producto = productos.stream()
					.filter(p -> p.getIdProducto().equals(detDto.getProducto().getIdProducto()))
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
		
		// Información del cliente
		if (salida.getCliente() != null) {
			dto.setIdCliente(salida.getCliente().getIdCliente());
			dto.setDniCliente(salida.getCliente().getDni());
			dto.setNombreCliente(salida.getCliente().getNombres() + " " + 
				(salida.getCliente().getApellidos() != null ? salida.getCliente().getApellidos() : ""));
		}
		
		dto.setTipoVenta(salida.getTipoVenta());
		
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
