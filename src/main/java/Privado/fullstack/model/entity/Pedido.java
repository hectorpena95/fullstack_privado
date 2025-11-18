
package Privado.fullstack.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Muchos a Uno con el Usuario (el Cliente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // Estado del pedido: PENDIENTE, ENVIADO, ENTREGADO, CANCELADO
    @Column(nullable = false, length = 20)
    private String estado;

    // Total final de la compra
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Relación Uno a Muchos con los detalles (productos) del pedido.
    // CascadeType.ALL asegura que si se elimina el Pedido, se eliminen sus detalles.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    /**
     * Método auxiliar para añadir un DetallePedido al Pedido.
     * Es crucial para asegurar que la relación bidireccional esté correctamente establecida.
     */
    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this); // Establece el vínculo inverso
    }
}