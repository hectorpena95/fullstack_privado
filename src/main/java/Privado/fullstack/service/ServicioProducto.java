
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

    // --- Métodos de Lectura (Catálogo Público) ---

    // Obtener todos los productos
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return repositorioProducto.findAll();
    }

    // Obtener un producto por ID
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        return repositorioProducto.findById(id);
    }

    // Buscar productos por nombre o categoría (puedes expandir esta lógica)
    @Transactional(readOnly = true)
    public List<Producto> buscarProductos(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        // Utiliza el método personalizado del repositorio
        return repositorioProducto.findByNombreContainingIgnoreCase(termino);
    }

    // --- Métodos de Gestión (Solo para ADMIN) ---

    // Crear o Actualizar un producto
    @Transactional
    public Producto guardarProducto(Producto producto) {
        // Validación de stock (asegurar que no sea negativo al crear/actualizar)
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        return repositorioProducto.save(producto);
    }

    // Eliminar un producto
    @Transactional
    public void eliminarProducto(Long id) {
        repositorioProducto.deleteById(id);
    }

    // Lógica para actualizar el stock después de un pedido
    @Transactional
    public void actualizarStock(Long idProducto, int cantidadVendida) {
        Producto producto = repositorioProducto.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para actualizar stock."));

        int nuevoStock = producto.getStock() - cantidadVendida;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente para la venta.");
        }

        producto.setStock(nuevoStock);
        repositorioProducto.save(producto);
    }
}