// Privado.fullstack.repository.RepositorioRol.java

package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioRol extends JpaRepository<Rol, Long> {

    /**
     * Método personalizado para buscar un Rol por su nombre.
     * Es crucial para la inicialización y asignación de roles de usuario.
     * @param name El nombre del rol (String), ej. "ROLE_CLIENT".
     * @return El Rol encontrado en un Optional.
     */
    Optional<Rol> findByName(String name);
}