package tienda.inventario.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoría no debe exceder 100 caracteres")
    private String nombre;

    @Column(length = 255)
    @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    // Relación con Producto (unidireccional en JSON para evitar recursión)
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // ✅ oculta los productos en la respuesta de categoría
    private List<Producto> productos;
}
