package Privado.fullstack.service;

import Privado.fullstack.config.UtilidadJwt;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.SolicitudRegistro;
import Privado.fullstack.model.entity.Rol;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioRol;
import Privado.fullstack.repository.RepositorioUsuario;
import org.springframework.context.annotation.Lazy; // 💡 Importante: Añadir la importación de @Lazy
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class ServicioAutenticacion implements UserDetailsService {

    public final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasena;

    // 🔑 Corrección 1: Usamos @Lazy en la inyección de AuthenticationManager para romper el ciclo.
    private final AuthenticationManager administradorAutenticacion;
    private final UtilidadJwt utilidadJwt;

    // Constructor con Inyección de Dependencias
    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol,
                                 PasswordEncoder codificadorContrasena,
                                 @Lazy AuthenticationManager administradorAutenticacion, // Aplicamos @Lazy aquí
                                 UtilidadJwt utilidadJwt) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasena = codificadorContrasena;
        this.administradorAutenticacion = administradorAutenticacion;
        this.utilidadJwt = utilidadJwt;
    }

    /**
     * Carga el usuario desde la base de datos por el nombre de usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repositorioUsuario.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el nombre de usuario: " + username));
    }

    /**
     * Registra un nuevo Usuario y le asigna el rol por defecto (CLIENTE).
     */
    @Transactional
    public Usuario registrarNuevoUsuario(SolicitudRegistro solicitud) {
        // 1. Validar que el usuario no exista
        if (repositorioUsuario.existsByUsername(solicitud.getUsername())) {
            throw new RuntimeException("El nombre de usuario '" + solicitud.getUsername() + "' ya está en uso.");
        }
        if (repositorioUsuario.existsByEmail(solicitud.getEmail())) {
            throw new RuntimeException("El email '" + solicitud.getEmail() + "' ya está en uso.");
        }

        // 2. Crear y configurar el nuevo Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(solicitud.getUsername());
        usuario.setEmail(solicitud.getEmail());
        usuario.setFullName(solicitud.getFullName());

        // Encriptar la contraseña antes de guardarla en la base de datos
        usuario.setPassword(codificadorContrasena.encode(solicitud.getPassword()));

        // 3. Asignar Rol por defecto (CLIENTE)
        final String NOMBRE_ROL_CLIENTE = "ROLE_CLIENT";
        Rol rolCliente = repositorioRol.findByName(NOMBRE_ROL_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: El rol de CLIENTE no fue encontrado en la base de datos."));

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
        // 1. Autenticar al usuario utilizando el AuthenticationManager de Spring Security
        Authentication authentication = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.getUsername(), solicitud.getPassword())
        );

        // 2. Si la autenticación es exitosa, generar el token JWT
        UserDetails detallesUsuario = (UserDetails) authentication.getPrincipal();
        return utilidadJwt.generarToken(detallesUsuario);
    }
}