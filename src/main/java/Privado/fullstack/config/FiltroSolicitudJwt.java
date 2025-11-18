package Privado.fullstack.config;

import Privado.fullstack.service.ServicioDetallesUsuario;
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
    private final ServicioDetallesUsuario servicioDetallesUsuario;

    public FiltroSolicitudJwt(UtilidadJwt utilidadJwt, ServicioDetallesUsuario servicioDetallesUsuario) {
        this.utilidadJwt = utilidadJwt;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String encabezadoAutorizacion = request.getHeader("Authorization");
        String username = null;
        String tokenJwt = null;

        // 1. Verificar y extraer el token JWT (debe empezar con "Bearer ")
        if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
            tokenJwt = encabezadoAutorizacion.substring(7);
            username = utilidadJwt.extraerUsername(tokenJwt);
        }

        // 2. Si el username es válido y no está autenticado actualmente
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar los detalles del usuario
            UserDetails detallesUsuario = this.servicioDetallesUsuario.loadUserByUsername(username);

            // 3. Validar el token
            if (utilidadJwt.validarToken(tokenJwt, detallesUsuario)) {

                // Si es válido, crear un objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        detallesUsuario, null, detallesUsuario.getAuthorities());

                // Asignar los detalles de la solicitud (IP, sesión, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}