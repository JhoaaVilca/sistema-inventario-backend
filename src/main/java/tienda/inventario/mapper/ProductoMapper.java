package tienda.inventario.mapper;

import tienda.inventario.dto.ProductoRequestDTO;
import tienda.inventario.dto.ProductoResponseDTO;
import tienda.inventario.modelo.Categoria;
import tienda.inventario.modelo.Producto;
import java.time.LocalDate;

public class ProductoMapper {

    public static Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        Producto p = new Producto();
        p.setNombreProducto(dto.getNombreProducto());
        p.setPrecio(dto.getPrecio()); // Precio de venta
        p.setPrecioCompra(dto.getPrecioCompra()); // Precio de compra
        p.setStock(dto.getStock());
        p.setStockMinimo(dto.getStockMinimo());
        p.setUnidadMedida(dto.getUnidadMedida());
        p.setFechaIngreso(dto.getFechaIngreso());
        p.setFechaVencimiento(dto.getFechaVencimiento());
        p.setEsPerecible(dto.getEsPerecible());
        p.setDescripcionCorta(dto.getDescripcionCorta());
        p.setCategoria(categoria);

        return p;
    }

    public static ProductoResponseDTO toResponse(Producto p) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        
        // Campos básicos
        dto.setIdProducto(p.getIdProducto());
        dto.setNombreProducto(p.getNombreProducto());
        dto.setPrecio(p.getPrecio()); // Precio de venta
        dto.setPrecioCompra(p.getPrecioCompra()); // Precio de compra
        dto.setStock(p.getStock());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setUnidadMedida(p.getUnidadMedida());
        dto.setFechaIngreso(p.getFechaIngreso());
        dto.setFechaVencimiento(p.getFechaVencimiento());
        dto.setEsPerecible(p.getEsPerecible());
        dto.setDescripcionCorta(p.getDescripcionCorta());
        
        // Relaciones
        dto.setIdCategoria(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null);
        dto.setNombreCategoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null);
        
        // Campos calculados para alertas
        dto.setStockBajo(calcularStockBajo(p));
        dto.setProximoVencer(calcularProximoVencer(p));
        dto.setVencido(calcularVencido(p));
        dto.setMargenGanancia(calcularMargenGanancia(p));
        
        return dto;
    }
    
    // Métodos auxiliares para cálculos
    private static Boolean calcularStockBajo(Producto p) {
        if (p.getStock() == null || p.getStockMinimo() == null) {
            return false;
        }
        return p.getStock() <= p.getStockMinimo();
    }
    
    private static Boolean calcularProximoVencer(Producto p) {
        if (!Boolean.TRUE.equals(p.getEsPerecible()) || p.getFechaVencimiento() == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(30); // Próximos 30 días
        return p.getFechaVencimiento().isAfter(hoy) && p.getFechaVencimiento().isBefore(fechaLimite);
    }
    
    private static Boolean calcularVencido(Producto p) {
        if (!Boolean.TRUE.equals(p.getEsPerecible()) || p.getFechaVencimiento() == null) {
            return false;
        }
        return p.getFechaVencimiento().isBefore(LocalDate.now());
    }
    
    private static Double calcularMargenGanancia(Producto p) {
        if (p.getPrecio() == null || p.getPrecioCompra() == null || p.getPrecioCompra() == 0) {
            return null;
        }
        return ((p.getPrecio() - p.getPrecioCompra()) / p.getPrecioCompra()) * 100;
    }
}
