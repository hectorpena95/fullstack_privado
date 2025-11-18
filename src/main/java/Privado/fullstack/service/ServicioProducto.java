// Privado.fullstack.service.ServicioProducto.java

package Privado.fullstack.service;

import Privado.fullstack.model.entity.Producto;
import Privado.fullstack.repository.RepositorioProducto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioProducto {

    private final RepositorioProducto repositorioProducto;

    public ServicioProducto(RepositorioProducto repositorioProducto) {
        this.repositorioProducto = repositorioProducto;
    }

    // --- Métodos de Lectura (Usados por el ControladorProducto y ServicioPedido) ---

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return repositorioProducto.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        // Usado por ServicioPedido para verificar la existencia y obtener el precio/stock.
        return repositorioProducto.findById(id);
    }

    // --- Métodos de Escritura (Usados por el ControladorProducto y ServicioPedido) ---

    @Transactional
    public Producto guardarProducto(Producto producto) {
        // Usado para crear (POST) o actualizar (PUT) productos.
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        return repositorioProducto.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        repositorioProducto.deleteById(id);
    }

    /**
     * Lógica crítica: Reduce el stock después de una compra exitosa.
     * @param productoId ID del producto comprado.
     * @param cantidadVendida La cantidad a restar del stock.
     */
    @Transactional
    public void actualizarStock(Long productoId, int cantidadVendida) {
        Producto producto = repositorioProducto.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para actualizar stock."));

        int nuevoStock = producto.getStock() - cantidadVendida;

        if (nuevoStock < 0) {
            // Este error ya debería ser capturado antes en ServicioPedido,
            // pero es una validación de seguridad extra.
            throw new RuntimeException("Error fatal: Intento de stock negativo.");
        }

        producto.setStock(nuevoStock);
        repositorioProducto.save(producto);
    }
}