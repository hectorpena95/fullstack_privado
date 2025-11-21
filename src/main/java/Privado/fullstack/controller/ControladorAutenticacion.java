package Privado.fullstack.controller;

import Privado.fullstack.model.dto.RespuestaLogin;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.dto.SolicitudRegistro;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.service.ServicioAutenticacion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final AuthenticationManager administradorAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion,
                                    AuthenticationManager administradorAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.administradorAutenticacion = administradorAutenticacion;
    }

    // =========================================
    // 1. REGISTRO
    // =========================================
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody SolicitudRegistro solicitud) {
        try {
            Usuario nuevoUsuario = servicioAutenticacion.registrarNuevoUsuario(solicitud);

            // Obtener UserDetails desde el email para generar token correcto
            UserDetails detallesUsuario = servicioAutenticacion.loadUserByUsername(nuevoUsuario.getEmail());

            RespuestaLogin respuesta = servicioAutenticacion.crearRespuestaLogin(detallesUsuario);

            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // =========================================
    // 2. LOGIN
    // =========================================
    @PostMapping("/autenticar")
    public ResponseEntity<RespuestaLogin> iniciarSesion(@RequestBody SolicitudLogin solicitud) {

        // 1. Autenticar
        Authentication auth = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(
                        solicitud.getEmail(),
                        solicitud.getPassword()
                )
        );

        // 2. Extraer UserDetails autenticado
        UserDetails detallesUsuario = (UserDetails) auth.getPrincipal();

        // 3. Crear el DTO de respuesta con token + username + rol
        RespuestaLogin respuesta = servicioAutenticacion.crearRespuestaLogin(detallesUsuario);

        return ResponseEntity.ok(respuesta);
    }
}
