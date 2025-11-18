package Privado.fullstack.controller;

import Privado.fullstack.model.dto.RespuestaLogin;
import Privado.fullstack.model.dto.SolicitudLogin;
import Privado.fullstack.model.SolicitudRegistro;
import Privado.fullstack.model.entity.Usuario;
import Privado.fullstack.service.ServicioAutenticacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth") // Ruta base para autenticación
public class ControladorAutenticacion {

    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    // 1. ENDPOINT DE REGISTRO
    // Ruta: POST /api/v1/auth/registrar
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody SolicitudRegistro solicitud) {
        try {
            Usuario nuevoUsuario = servicioAutenticacion.registrarNuevoUsuario(solicitud);
            return new ResponseEntity<>("Usuario registrado exitosamente como: " + nuevoUsuario.getUsername(), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Manejar errores como "El usuario ya existe"
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. ENDPOINT DE LOGIN
    // Ruta: POST /api/v1/auth/iniciar
    @PostMapping("/iniciar")
    public ResponseEntity<RespuestaLogin> iniciarSesion(@RequestBody SolicitudLogin solicitud) {

        // 1. Autenticar y generar el token
        String tokenJwt = servicioAutenticacion.autenticarYGenerarToken(solicitud);

        // 2. Buscar al usuario para obtener roles y detalles
        Usuario usuario = servicioAutenticacion.repositorioUsuario.findByUsername(solicitud.getUsername()).orElseThrow();

        // 3. Obtener el rol principal (usaremos el primero que encontremos, asumiendo un rol por usuario)
        String rolPrincipal = usuario.getRoles().stream()
                .map(rol -> rol.getName().name())
                .collect(Collectors.joining(",")); // Si tiene múltiples roles, los une con coma

        // 4. Construir la respuesta para el Frontend
        RespuestaLogin respuesta = new RespuestaLogin();
        respuesta.setToken(tokenJwt);
        respuesta.setUsername(usuario.getUsername());
        respuesta.setRol(rolPrincipal);
        // respuesta.setTipo("Bearer"); // Ya está por defecto en el DTO

        return ResponseEntity.ok(respuesta);
    }
}