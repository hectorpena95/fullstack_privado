
package Privado.fullstack.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data // Genera getters, setters, equals, hashCode y toString (Lombok)
@NoArgsConstructor // Constructor sin argumentos (JPA)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio; // Usar BigDecimal para precisión monetaria

    @Column(nullable = false)
    private Integer stock; // Cantidad disponible en inventario

    private String urlImagen; // URL o path de la imagen del producto

    // Opcional: Puede ser útil para ordenar o filtrar
    private String categoria;

    // Campos de auditoría simple
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion;

    // Antes de guardar (PERSIST) o actualizar (UPDATE), se ejecuta este método
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}