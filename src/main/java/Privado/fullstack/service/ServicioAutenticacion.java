package Privado.fullstack.service;


import Privado.fullstack.config.UtilidadJwt;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.SolicitudRegistro;
import Privado.fullstack.model.entity.Rol;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.model.enums.RolEnum;
import Privado.fullstack.repository.RepositorioRol;
import Privado.fullstack.repository.RepositorioUsuario;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class ServicioAutenticacion {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasena;
    private final AuthenticationManager administradorAutenticacion;
    private final UtilidadJwt utilidadJwt;

    // Constructor con Inyección de Dependencias
    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol,
                                 PasswordEncoder codificadorContrasena, AuthenticationManager administradorAutenticacion,
                                 UtilidadJwt utilidadJwt) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasena = codificadorContrasena;
        this.administradorAutenticacion = administradorAutenticacion;
        this.utilidadJwt = utilidadJwt;
    }

    /**
     * Registra un nuevo Usuario y le asigna el rol por defecto (CLIENTE).
     */
    @Transactional
    public Usuario registrarNuevoUsuario(SolicitudRegistro solicitud) {
        // 1. Validar que el usuario no exista
        if (repositorioUsuario.existsByUsername(solicitud.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
        if (repositorioUsuario.existsByEmail(solicitud.getEmail())) {
            throw new RuntimeException("El email ya está en uso.");
        }

        // 2. Crear y configurar el nuevo Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(solicitud.getUsername());
        usuario.setEmail(solicitud.getEmail());
        usuario.setFullName(solicitud.getFullName());
        // ENCRIPTAR CONTRASEÑA
        usuario.setPassword(codificadorContrasena.encode(solicitud.getPassword()));

        // 3. Asignar Rol por defecto (CLIENTE)
        Rol rolCliente = repositorioRol.findByName(RolEnum.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Error: Rol de CLIENTE no encontrado."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);
        usuario.setRoles(roles);

        // 4. Guardar en la base de datos
        return repositorioUsuario.save(usuario);
    }

    /**
     * Procesa la solicitud de login y genera un token JWT.
     */
    public String autenticarYGenerarToken(SolicitudLogin solicitud) {
        // 1. Autenticar usando Spring Security
        Authentication authentication = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.getUsername(), solicitud.getPassword())
        );

        // 2. Si la autenticación es exitosa, generar el token
        UserDetails detallesUsuario = (UserDetails) authentication.getPrincipal();
        return utilidadJwt.generarToken(detallesUsuario);
    }
}