package tienda.inventario.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tienda.inventario.modelo.Producto;
import java.util.List;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreProductoContainingIgnoreCase(String nombre);
    long countByStockGreaterThan(int min);
    long countByStockEquals(int value);
    long countByStockLessThanEqual(int value);
}
