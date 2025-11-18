
package Privado.fullstack.controller;

import Privado.fullstack.model.dto.SolicitudPedido;
import Privado.fullstack.model.entity.Pedido;
import Privado.fullstack.service.ServicioPedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class ControladorPedido {

    private final ServicioPedido servicioPedido;

    public ControladorPedido(ServicioPedido servicioPedido) {
        this.servicioPedido = servicioPedido;
    }

    // --- ENDPOINT DE CREACIÓN (CLIENTE) ---

    // POST /api/v1/pedidos
    // Requiere ROLE_CLIENT (o cualquier rol autenticado, ya cubierto por la ruta base)
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<?> crearPedido(
            @Valid @RequestBody SolicitudPedido solicitud, // @Valid activa la validación de DTOs
            Authentication authentication // Spring inyecta la información del usuario autenticado
    ) {
        try {
            // Obtener el nombre de usuario (principal) del token JWT
            String usernameCliente = authentication.getName();

            // Delegar la lógica transaccional al servicio
            Pedido nuevoPedido = servicioPedido.crearPedido(solicitud, usernameCliente);

            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            // Maneja errores como "Stock insuficiente" o "Producto no encontrado"
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // --- ENDPOINTS DE CONSULTA (ADMIN/VENDOR/CLIENT) ---

    // GET /api/v1/pedidos/mis-pedidos
    // Permite al cliente ver solo sus propios pedidos.
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> obtenerMisPedidos(Authentication authentication) {
        // Necesitas buscar el ID del usuario.
        // Asumiendo que ServicioDetallesUsuario o RepositorioUsuario puede ayudarte a encontrar el ID
        // *** NOTA: Necesitarías un método en ServicioUsuario para obtener el ID por username ***

        // Simulación: Asumimos que podemos obtener el ID del usuario (esto requiere una implementación adicional)
        // Por ahora, solo devolvemos una lista vacía para evitar errores de compilación complejos.
        // List<Pedido> pedidos = servicioPedido.obtenerPedidosCliente(idUsuarioReal);
        // return ResponseEntity.ok(pedidos);

        return ResponseEntity.ok(List.of());
    }

    // GET /api/v1/pedidos (Para la gestión)
    // Permite a ADMIN y VENDOR ver todos los pedidos en el sistema.
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidos() {
        List<Pedido> pedidos = servicioPedido.obtenerTodosLosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    // [PENDIENTE: Implementar PUT/PATCH para actualizar el estado del pedido (ej. de PENDIENTE a ENVIADO) - Solo ADMIN/VENDOR]
}