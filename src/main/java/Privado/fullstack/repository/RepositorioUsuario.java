package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    /**
     * Requerido por ServicioAutenticacion.loadUserByUsername() y login.
     * Busca un usuario por su nombre de usuario.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Requerido por ServicioAutenticacion.registrarNuevoUsuario() para validación.
     * Verifica si un nombre de usuario ya existe.
     */
    Boolean existsByUsername(String username);

    /**
     * Requerido por ServicioAutenticacion.registrarNuevoUsuario() para validación.
     * Verifica si un email ya existe.
     */
    Boolean existsByEmail(String email);
}