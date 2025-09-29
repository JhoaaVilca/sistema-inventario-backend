package tienda.inventario.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import tienda.inventario.modelo.DetalleEntrada;
import tienda.inventario.modelo.Entrada;
import tienda.inventario.modelo.Producto;
import tienda.inventario.modelo.Lote;
import tienda.inventario.repositorio.DetalleEntradaRepositorio;
import tienda.inventario.repositorio.EntradaRepositorio;
import tienda.inventario.repositorio.ProductoRepositorio;
import tienda.inventario.repositorio.LoteRepositorio;

import java.time.LocalDate;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class EntradaServicio implements IEntradaServicio {

    @Autowired
    private EntradaRepositorio entradaRepositorio;

    @Autowired
    private DetalleEntradaRepositorio detalleRepositorio;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private LoteRepositorio loteRepositorio;

    @Override
    public Entrada guardarEntrada(Entrada entrada) {
        if (entrada.getDetalles() != null) {
            for (DetalleEntrada detalle : entrada.getDetalles()) {
                detalle.setEntrada(entrada);
            }
        }
        Entrada nuevaEntrada = entradaRepositorio.save(entrada);

        if (nuevaEntrada.getDetalles() != null) {
            for (DetalleEntrada detalle : nuevaEntrada.getDetalles()) {
                Producto productoBD = productoRepositorio.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (productoBD != null) {
                    Integer stockActual = productoBD.getStock() != null ? productoBD.getStock() : 0;
                    productoBD.setStock(stockActual + detalle.getCantidad());
                    productoRepositorio.save(productoBD);
                }

                // Crear lote automáticamente si hay fecha de vencimiento
                if (detalle.getFechaVencimiento() != null) {
                    Lote lote = new Lote();
                    lote.setDetalleEntrada(detalle);
                    lote.setFechaEntrada(nuevaEntrada.getFechaEntrada()); // ✅ AGREGADO: Establecer fecha de entrada
                    lote.setFechaVencimiento(detalle.getFechaVencimiento());
                    lote.setNumeroLote("LOTE-" + detalle.getFechaVencimiento().toString().replace("-", ""));
                    lote.setEstado("Activo");
                    loteRepositorio.save(lote);
                }
            }
        }
        return nuevaEntrada;
    }

    @Override
    public List<Entrada> listarEntradas() {
        return entradaRepositorio.findAll();
    }

    @Override
    public Page<Entrada> listarEntradas(Pageable pageable) {
        return entradaRepositorio.findAll(pageable);
    }

    @Override
    public Entrada actualizarEntrada(Long id, Entrada entrada) {
        return entradaRepositorio.findById(id).map(entradaExistente -> {
            entradaExistente.setProveedor(entrada.getProveedor());
            entradaExistente.setFechaEntrada(entrada.getFechaEntrada());
            entradaExistente.setTotalEntrada(entrada.getTotalEntrada());
            entradaExistente.setEstado(entrada.getEstado()); // ✅ AGREGADO: Actualizar estado
            entradaExistente.setNumeroFactura(entrada.getNumeroFactura()); // ✅ AGREGADO: Actualizar número de factura
            entradaExistente.setObservaciones(entrada.getObservaciones()); // ✅ AGREGADO: Actualizar observaciones

            detalleRepositorio.deleteAll(entradaExistente.getDetalles());
            if (entrada.getDetalles() != null) {
                for (DetalleEntrada detalle : entrada.getDetalles()) {
                    detalle.setEntrada(entradaExistente);
                }
            }
            entradaExistente.setDetalles(entrada.getDetalles());

            return entradaRepositorio.save(entradaExistente);
        }).orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
    }

    @Override
    public void eliminarEntrada(Long id) {
        Entrada entrada = entradaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));

        if (entrada.getDetalles() != null) {
            for (DetalleEntrada detalle : entrada.getDetalles()) {
                Producto producto = productoRepositorio.findById(detalle.getProducto().getIdProducto())
                        .orElse(null);
                if (producto != null) {
                    producto.setStock(producto.getStock() - detalle.getCantidad());
                    productoRepositorio.save(producto);
                }
            }
        }
        entradaRepositorio.deleteById(id);
    }

    @Override
    public List<Entrada> filtrarPorProveedor(Long idProveedor) {
        return entradaRepositorio.findByProveedorId(idProveedor);
    }

    @Override
    public List<Entrada> filtrarPorFecha(LocalDate fecha) {
        return entradaRepositorio.findByFechaEntrada(fecha);
    }

    @Override
    public List<Entrada> filtrarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return entradaRepositorio.findByFechaEntradaBetween(fechaInicio, fechaFin);
    }

    @Override
    public List<Entrada> filtrarPorProveedorYRangoFechas(Long idProveedor, LocalDate fechaInicio, LocalDate fechaFin) {
        return entradaRepositorio.findByProveedorIdAndFechaEntradaBetween(idProveedor, fechaInicio, fechaFin);
    }

    @Override
    public List<Entrada> filtrarPorNumeroFactura(String numeroFactura) {
        return entradaRepositorio.findByNumeroFacturaContaining(numeroFactura);
    }

    @Override
    public List<Entrada> filtrarPorProveedorYNumeroFactura(Long idProveedor, String numeroFactura) {
        return entradaRepositorio.findByProveedorIdAndNumeroFacturaContaining(idProveedor, numeroFactura);
    }

    @Override
    public List<Entrada> filtrarPorProveedorYNumeroFacturaYRangoFechas(Long idProveedor, String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin) {
        return entradaRepositorio.findByProveedorIdAndNumeroFacturaContainingAndFechaEntradaBetween(idProveedor, numeroFactura, fechaInicio, fechaFin);
    }

    @Override
    public List<Entrada> filtrarPorNumeroFacturaYRangoFechas(String numeroFactura, LocalDate fechaInicio, LocalDate fechaFin) {
        return entradaRepositorio.findByNumeroFacturaContainingAndFechaEntradaBetween(numeroFactura, fechaInicio, fechaFin);
    }

    @Override
    public String subirFactura(Long idEntrada, MultipartFile file) {
        try {
            // Verificar que la entrada existe
            Entrada entrada = entradaRepositorio.findById(idEntrada)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada con ID: " + idEntrada));

            // Crear directorio de uploads si no existe
            Path uploadDir = Paths.get("uploads/facturas");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new RuntimeException("El nombre del archivo no puede ser nulo o vacío");
            }
            
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }
            
            String filename = "factura_" + idEntrada + "_" + UUID.randomUUID().toString() + extension;
            
            // Guardar archivo
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar entrada con la URL de la factura
            String facturaUrl = "/api/entradas/" + idEntrada + "/factura";
            entrada.setFacturaUrl(facturaUrl);
            entradaRepositorio.save(entrada);

            return facturaUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error al subir la factura: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource descargarFactura(Long idEntrada) {
        try {
            Entrada entrada = entradaRepositorio.findById(idEntrada)
                .orElseThrow(() -> new RuntimeException("Entrada no encontrada con ID: " + idEntrada));

            if (entrada.getFacturaUrl() == null) {
                return null;
            }

            // Buscar archivo en el directorio de uploads
            Path uploadDir = Paths.get("uploads/facturas");
            
            // Buscar archivo que coincida con el patrón
            try {
                return Files.list(uploadDir)
                    .filter(path -> path.getFileName().toString().startsWith("factura_" + idEntrada + "_"))
                    .findFirst()
                    .map(path -> {
                        try {
                            return new UrlResource(path.toUri());
                        } catch (Exception e) {
                            throw new RuntimeException("Error al cargar el archivo", e);
                        }
                    })
                    .orElse(null);
            } catch (Exception e) {
                throw new RuntimeException("Error al buscar la factura", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al descargar la factura: " + e.getMessage(), e);
        }
    }
}
