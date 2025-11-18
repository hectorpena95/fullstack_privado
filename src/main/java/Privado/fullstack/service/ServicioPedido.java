
package Privado.fullstack.service;

import Privado.fullstack.model.dto.DetalleSolicitudPedido;
import Privado.fullstack.model.dto.SolicitudPedido;
import Privado.fullstack.model.entity.DetallePedido;
import Privado.fullstack.model.entity.Pedido;
import Privado.fullstack.model.entity.Producto;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioPedido;
import Privado.fullstack.repository.RepositorioUsuario;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioPedido {

    private final RepositorioPedido repositorioPedido;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioProducto servicioProducto;

    // Asumiendo que RepositorioPedido y ServicioProducto existen
    public ServicioPedido(RepositorioPedido repositorioPedido, RepositorioUsuario repositorioUsuario, ServicioProducto servicioProducto) {
        this.repositorioPedido = repositorioPedido;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioProducto = servicioProducto;
    }

    /**
     * Procesa la creación de un nuevo pedido.
     */
    @Transactional
    public Pedido crearPedido(SolicitudPedido solicitud, String username) {

        // 1. Obtener el Usuario (Cliente) autenticado
        Usuario cliente = repositorioUsuario.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setEstado("PENDIENTE");

        BigDecimal totalPedido = BigDecimal.ZERO;

        // 2. Procesar cada detalle de la orden
        for (DetalleSolicitudPedido detalleSolicitud : solicitud.getDetalles()) {

            Producto producto = servicioProducto.obtenerPorId(detalleSolicitud.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto con ID " + detalleSolicitud.getIdProducto() + " no existe."));

            int cantidad = detalleSolicitud.getCantidad();

            // 3. Validar y Actualizar Stock (Lógica crucial)
            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Actualizar el stock
            servicioProducto.actualizarStock(producto.getId(), cantidad);

            // 4. Crear el DetallePedido
            DetallePedido detallePedido = new DetallePedido();
            detallePedido.setProducto(producto);
            detallePedido.setCantidad(cantidad);
            detallePedido.setPrecioUnitario(producto.getPrecio());

            nuevoPedido.agregarDetalle(detallePedido); // Asume que Pedido tiene un método agregarDetalle

            totalPedido = totalPedido.add(detallePedido.getSubtotal());
        }

        // 5. Finalizar y guardar el Pedido
        nuevoPedido.setTotal(totalPedido);

        return repositorioPedido.save(nuevoPedido);
    }

    // Método para obtener todos los pedidos (Usado por ADMIN/VENDOR)
    @Transactional(readOnly = true)
    public List<Pedido> obtenerTodosLosPedidos() {
        return repositorioPedido.findAll();
    }

    // Método para obtener los pedidos de un usuario (Usado por CLIENTE)
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosPorUsername(String username) {

        Usuario cliente = repositorioUsuario.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado con nombre: " + username));

        // Asumiendo que RepositorioPedido tiene el método findByClienteId
        return repositorioPedido.findByClienteId(cliente.getId());
    }
}