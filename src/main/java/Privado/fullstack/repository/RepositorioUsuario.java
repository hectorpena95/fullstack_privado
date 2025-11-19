package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    /**
     * Requerido por ServicioAutenticacion.loadUserByUsername() y la lógica de login.
     * Busca un usuario por su email.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su nombre de usuario (el campo 'nombre' del DTO en la BD).
     * No es usado en el login actual, pero es estándar para búsquedas.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Requerido por ServicioAutenticacion.registrarNuevoUsuario() para validación.
     * Verifica si un nombre de usuario (el 'nombre' del DTO) ya existe.
     */
    Boolean existsByUsername(String username);

    /**
     * Requerido por ServicioAutenticacion.registrarNuevoUsuario() para validación.
     * Verifica si un email ya existe.
     */
    Boolean existsByEmail(String email);
}