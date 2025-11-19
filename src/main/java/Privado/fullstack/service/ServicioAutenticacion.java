package Privado.fullstack.service;

import Privado.fullstack.config.UtilidadJwt;
import Privado.fullstack.model.dto.RespuestaLogin;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.dto.SolicitudRegistro;
import Privado.fullstack.model.entity.Rol;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.repository.RepositorioRol;
import Privado.fullstack.repository.RepositorioUsuario;
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
import java.util.stream.Collectors;

@Service
public class ServicioAutenticacion implements UserDetailsService {

    public final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasena;
    private final UtilidadJwt utilidadJwt;

    // El constructor NO inyecta AuthenticationManager, rompiendo el ciclo.
    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol,
                                 PasswordEncoder codificadorContrasena,
                                 UtilidadJwt utilidadJwt) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasena = codificadorContrasena;
        this.utilidadJwt = utilidadJwt;
    }

    /**
     * Carga el usuario desde la base de datos por el email.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
    }

    /**
     * Registra un nuevo Usuario y le asigna el rol por defecto (CLIENTE).
     */
    @Transactional
    public Usuario registrarNuevoUsuario(SolicitudRegistro solicitud) {

        if (repositorioUsuario.existsByUsername(solicitud.getNombre())) {
            throw new RuntimeException("El nombre de usuario '" + solicitud.getNombre() + "' ya está en uso.");
        }
        if (repositorioUsuario.existsByEmail(solicitud.getEmail())) {
            throw new RuntimeException("El email '" + solicitud.getEmail() + "' ya está en uso.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(solicitud.getNombre());
        usuario.setEmail(solicitud.getEmail());
        usuario.setPassword(codificadorContrasena.encode(solicitud.getPassword()));

        final String NOMBRE_ROL_CLIENTE = "ROLE_CLIENT";
        Rol rolCliente = repositorioRol.findByName(NOMBRE_ROL_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: El rol de CLIENTE no fue encontrado en la base de datos."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);
        usuario.setRoles(roles);

        return repositorioUsuario.save(usuario);
    }

    /**
     * Procesa la solicitud de login y genera un token JWT.
     * @param administradorAutenticacion Inyectado en el controlador y pasado aquí.
     */
    public String autenticarYGenerarToken(SolicitudLogin solicitud, AuthenticationManager administradorAutenticacion) {
        // 1. Autenticar al usuario utilizando el email (como identificador) y la contraseña.
        Authentication authentication = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.getEmail(), solicitud.getPassword())
        );

        // 2. Si la autenticación es exitosa, generar el token JWT
        UserDetails detallesUsuario = (UserDetails) authentication.getPrincipal();
        return utilidadJwt.generarToken(detallesUsuario);
    }

    /**
     * Crea el DTO de respuesta para el Controlador.
     */
    public RespuestaLogin crearRespuestaLogin(Usuario usuario) {

        String tokenJwt = utilidadJwt.generarToken(usuario);

        String rolPrincipal = usuario.getRoles().stream()
                .map(Rol::getName)
                .collect(Collectors.joining(","));

        RespuestaLogin respuesta = new RespuestaLogin();
        respuesta.setToken(tokenJwt);
        respuesta.setUsername(usuario.getUsername());
        respuesta.setRol(rolPrincipal);

        return respuesta;
    }
}