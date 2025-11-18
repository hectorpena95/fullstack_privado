
package Privado.fullstack.repository;

import Privado.fullstack.model.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioRol extends JpaRepository<Rol, Long> {

    /**
     * Busca un Rol por su nombre. 
     * Este método es vital para:
     * 1. El Inicializador de Datos (al inicio de la aplicación).
     * 2. El Servicio de Autenticación (al asignar el rol por defecto 'CLIENT').
     * @param name El nombre del rol (e.g., "ROLE_CLIENT").
     * @return Un Optional que contiene el Rol si existe.
     */
    Optional<Rol> findByName(String name);
}