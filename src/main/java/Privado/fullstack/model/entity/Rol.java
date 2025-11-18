
package Privado.fullstack.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cambiado de RolEnum a String para evitar el uso de enum
    @Column(length = 20, nullable = false, unique = true)
    private String name;

    // Constructor de conveniencia
    public Rol(String name) {
        this.name = name;
    }
}