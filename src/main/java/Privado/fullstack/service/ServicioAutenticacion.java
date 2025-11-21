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

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasena;
    private final UtilidadJwt utilidadJwt;

    public ServicioAutenticacion(
            RepositorioUsuario repositorioUsuario,
            RepositorioRol repositorioRol,
            PasswordEncoder codificadorContrasena,
            UtilidadJwt utilidadJwt) {

        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasena = codificadorContrasena;
        this.utilidadJwt = utilidadJwt;
    }

    // ==========================
    //  CARGAR USUARIO POR EMAIL
    // ==========================
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
    }

    // ==========================
    //   REGISTRO DE USUARIO
    // ==========================
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
                .orElseThrow(() -> new RuntimeException("Error: El rol de CLIENTE no fue encontrado."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);
        usuario.setRoles(roles);

        return repositorioUsuario.save(usuario);
    }

    // ==========================
    //   AUTENTICAR & GENERAR TOKEN
    // ==========================
    public String autenticarYGenerarToken(SolicitudLogin solicitud, AuthenticationManager administradorAutenticacion) {

        // Autenticación con email + password
        Authentication authentication = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(
                        solicitud.getEmail(),
                        solicitud.getPassword()
                )
        );

        // Usuario autenticado (UserDetails de Spring Security)
        UserDetails detallesUsuario = (UserDetails) authentication.getPrincipal();

        // Generar token JWT correcto
        return utilidadJwt.generarToken(detallesUsuario);
    }

    // ==========================
    //   DTO DE RESPUESTA LOGIN
    // ==========================
    public RespuestaLogin crearRespuestaLogin(UserDetails userDetails) {

        String tokenJwt = utilidadJwt.generarToken(userDetails);

        String rolPrincipal = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.joining(","));

        RespuestaLogin respuesta = new RespuestaLogin();
        respuesta.setToken(tokenJwt);
        respuesta.setUsername(userDetails.getUsername());
        respuesta.setRol(rolPrincipal);

        return respuesta;
    }
}
