
package Privado.fullstack.controller;

import Privado.fullstack.model.entity.Producto;
import Privado.fullstack.service.ServicioProducto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ControladorProducto {

    private final ServicioProducto servicioProducto;

    public ControladorProducto(ServicioProducto servicioProducto) {
        this.servicioProducto = servicioProducto;
    }

    // --- ENDPOINTS PÚBLICOS (Catálogo) ---

    // GET /api/v1/productos (Listar todos)
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        List<Producto> productos = servicioProducto.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    // GET /api/v1/productos/{id} (Ver detalle)
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerDetalle(@PathVariable Long id) {
        // CORRECCIÓN: Usamos ResponseEntity.of() para manejar el Optional de manera limpia.
        // Devuelve 200 OK si el Optional tiene valor, 404 NOT FOUND si está vacío.
        return ResponseEntity.of(servicioProducto.obtenerPorId(id));
    }

    // --- ENDPOINTS PROTEGIDOS (Gestión - ADMIN/VENDOR) ---

    // POST /api/v1/productos (Crear nuevo producto)
    // Permite a ADMIN y VENDOR crear productos.
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevoProducto = servicioProducto.guardarProducto(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Error si el stock es negativo, por ejemplo.
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/v1/productos/{id} (Actualizar producto existente)
    // Permite a ADMIN y VENDOR actualizar.
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return servicioProducto.obtenerPorId(id)
                .map(productoExistente -> {
                    // Actualización de campos
                    productoExistente.setNombre(producto.getNombre());
                    productoExistente.setDescripcion(producto.getDescripcion());
                    productoExistente.setPrecio(producto.getPrecio());
                    productoExistente.setStock(producto.getStock());
                    productoExistente.setCategoria(producto.getCategoria());

                    try {
                        Producto actualizado = servicioProducto.guardarProducto(productoExistente);
                        return ResponseEntity.ok(actualizado);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().build();
                    }
                })
                // CORRECCIÓN: Usamos orElseGet para manejar el 404 y devolver el tipo correcto.
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/v1/productos/{id} (Eliminar producto)
    // Solo ADMIN puede eliminar (por precaución).
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (servicioProducto.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        servicioProducto.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}