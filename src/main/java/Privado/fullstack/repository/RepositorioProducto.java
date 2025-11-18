
package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioProducto extends JpaRepository<Producto, Long> {

    // Método personalizado: Buscar productos por nombre (ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Método personalizado: Buscar productos por categoría
    List<Producto> findByCategoria(String categoria);
}