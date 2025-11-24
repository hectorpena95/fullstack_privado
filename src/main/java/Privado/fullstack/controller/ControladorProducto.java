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
@CrossOrigin(origins = "http://localhost:5173") // 👈 HABILITAR CORS PARA REACT
public class ControladorProducto {

    private final ServicioProducto servicioProducto;

    public ControladorProducto(ServicioProducto servicioProducto) {
        this.servicioProducto = servicioProducto;
    }

    // ============================
    // 🚀 ENDPOINTS PÚBLICOS (sin JWT)
    // ============================

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(servicioProducto.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.of(servicioProducto.obtenerPorId(id));
    }

    // ============================
    // 🔐 ENDPOINTS SOLO ADMIN
    // ============================

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
                .map(existente -> {
                    existente.setNombre(producto.getNombre());
                    existente.setDescripcion(producto.getDescripcion());
                    existente.setPrecio(producto.getPrecio());
                    existente.setStock(producto.getStock());
                    existente.setCategoria(producto.getCategoria());
                    existente.setUrlImagen(producto.getUrlImagen());

                    return ResponseEntity.ok(servicioProducto.guardarProducto(existente));
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
