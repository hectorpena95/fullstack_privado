package Privado.fullstack.config;

import Privado.fullstack.service.ServicioAutenticacion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroSolicitudJwt extends OncePerRequestFilter {

    private final UtilidadJwt utilidadJwt;
    // CORRECCIÓN 2: Usar ServicioAutenticacion
    private final ServicioAutenticacion servicioAutenticacion;

    // Constructor con Inyección de Dependencias
    public FiltroSolicitudJwt(UtilidadJwt utilidadJwt, ServicioAutenticacion servicioAutenticacion) {
        this.utilidadJwt = utilidadJwt;
        this.servicioAutenticacion = servicioAutenticacion; // Inyectar el servicio de autenticación
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String encabezadoAutorizacion = request.getHeader("Authorization");
        String username = null;
        String tokenJwt = null;

        // 1. Verificar y extraer el token JWT
        if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
            tokenJwt = encabezadoAutorizacion.substring(7);
            username = utilidadJwt.extraerUsername(tokenJwt);
        }

        // 2. Si el username es válido y no está autenticado
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // CORRECCIÓN 3: El ServicioAutenticacion debe implementar UserDetailsService,
            // y por lo tanto, debe tener el método loadUserByUsername().
            UserDetails detallesUsuario = this.servicioAutenticacion.loadUserByUsername(username);

            // 3. Validar el token
            if (utilidadJwt.validarToken(tokenJwt, detallesUsuario)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        detallesUsuario, null, detallesUsuario.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Establecer la autenticación
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}