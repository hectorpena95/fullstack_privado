// Privado.fullstack.repository.RepositorioProducto.java

package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioProducto extends JpaRepository<Producto, Long> {

    // Método opcional: Buscar productos por nombre (útil para la administración o el catálogo)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Método opcional: Buscar por stock
    List<Producto> findByStockGreaterThan(Integer stock);
}