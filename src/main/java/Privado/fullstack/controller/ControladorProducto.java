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

    // --- ENDPOINTS PÚBLICOS ---

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        List<Producto> productos = servicioProducto.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.of(servicioProducto.obtenerPorId(id));
    }

    // --- ENDPOINTS SOLO ADMIN ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevoProducto = servicioProducto.guardarProducto(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return servicioProducto.obtenerPorId(id)
                .map(productoExistente -> {
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
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

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
