package tienda.inventario.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "productos") // 🚫 evita bucles en toString
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    private String nombre;

    // Relación con Producto (unidireccional en JSON para evitar recursión)
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // ✅ oculta los productos en la respuesta de categoría
    private List<Producto> productos;
}
