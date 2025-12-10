package Privado.fullstack.controller;

import Privado.fullstack.model.entity.Producto;
import Privado.fullstack.model.entity.ProductoFisico;
import Privado.fullstack.model.entity.ProductoDigital;
import Privado.fullstack.service.ServicioProducto;
import Privado.fullstack.service.ProductoPolimorfismoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorProducto {

    private final ServicioProducto servicioProducto;
    private final ProductoPolimorfismoService polimorfismoService;

    public ControladorProducto(ServicioProducto servicioProducto,
                               ProductoPolimorfismoService polimorfismoService) {
        this.servicioProducto = servicioProducto;
        this.polimorfismoService = polimorfismoService;
    }

    // =========================================================
    // 🚀 ENDPOINT DE POO (HERENCIA + POLIMORFISMO)
    // =========================================================
    @GetMapping("/poo")
    public String probarPOO() {

        ProductoFisico mouse = new ProductoFisico(
                1L,
                "Mouse Gamer RGB",
                new BigDecimal("19990")
        );

        ProductoDigital skin = new ProductoDigital(
                2L,
                "Skin Legendaria",
                new BigDecimal("4990")
        );

        String r1 = polimorfismoService.describirProducto(mouse);
        String r2 = polimorfismoService.describirProducto(skin);

        return r1 + " || " + r2;
    }

    // =========================================================
    // 🚀 ENDPOINTS PÚBLICOS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(servicioProducto.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.of(servicioProducto.obtenerPorId(id));
    }

    // =========================================================
    // 🔐 ENDPOINTS PRIVADOS (ADMIN + VENDEDOR)
    // =========================================================

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDEDOR')")
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = servicioProducto.guardarProducto(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDEDOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id,
                                                @RequestBody Producto producto) {

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

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDEDOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {

        if (servicioProducto.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        servicioProducto.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

}
