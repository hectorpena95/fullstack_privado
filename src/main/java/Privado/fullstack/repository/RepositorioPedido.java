
package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioPedido extends JpaRepository<Pedido, Long> {

    /**
     * Requerido por ServicioPedido.obtenerPedidosPorUsername().
     * Spring Data JPA automáticamente implementa este método
     * buscando todos los pedidos donde el campo 'cliente' tiene el 'id' especificado.
     * * @param clienteId El ID del usuario (cliente).
     * @return Una lista de todos los pedidos realizados por ese cliente.
     */
    List<Pedido> findByClienteId(Long clienteId);
}