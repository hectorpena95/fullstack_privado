package Privado.fullstack.controller;

import Privado.fullstack.model.dto.RespuestaLogin;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.dto.SolicitudRegistro;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.service.ServicioAutenticacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Importación necesaria
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth") // Ruta base para autenticación
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final AuthenticationManager administradorAutenticacion;

    // Constructor con Inyección de Dependencias
    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion,
                                    AuthenticationManager administradorAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.administradorAutenticacion = administradorAutenticacion;
    }

    // 1. ENDPOINT DE REGISTRO
    // Ruta: POST /api/v1/auth/registrar
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody SolicitudRegistro solicitud) {
        try {
            Usuario nuevoUsuario = servicioAutenticacion.registrarNuevoUsuario(solicitud);
            // El usuario fue creado, generamos el DTO de respuesta que incluye el token
            RespuestaLogin respuesta = servicioAutenticacion.crearRespuestaLogin(nuevoUsuario);
            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Manejar errores como "El usuario ya existe"
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. ENDPOINT DE LOGIN
    // Ruta: POST /api/v1/auth/autenticar
    @PostMapping("/autenticar")
    public ResponseEntity<RespuestaLogin> iniciarSesion(@RequestBody SolicitudLogin solicitud) {

        // 1. Autenticar y generar el token, pasando el AuthenticationManager
        // 🔑 CORRECCIÓN 3: Pasamos el administradorAutenticacion como argumento
        String tokenJwt = servicioAutenticacion.autenticarYGenerarToken(solicitud, administradorAutenticacion);

        // 2. Buscar al usuario (usando el email) para obtener roles y detalles
        // NOTA: Asumimos que el UserDetails retornado es casteable a Usuario.
        Usuario usuario = (Usuario) servicioAutenticacion.loadUserByUsername(solicitud.getEmail());

        // 3. Obtener el rol principal
        String rolPrincipal = usuario.getRoles().stream()
                .map(rol -> rol.getName())
                .collect(Collectors.joining(","));

        // 4. Construir la respuesta para el Frontend
        RespuestaLogin respuesta = new RespuestaLogin();
        respuesta.setToken(tokenJwt);
        respuesta.setUsername(usuario.getUsername());
        respuesta.setRol(rolPrincipal);

        return ResponseEntity.ok(respuesta);
    }
}