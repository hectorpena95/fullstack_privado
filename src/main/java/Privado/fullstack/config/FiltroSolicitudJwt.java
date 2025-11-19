package Privado.fullstack.config;

import Privado.fullstack.service.ServicioAutenticacion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
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
    private final ServicioAutenticacion servicioAutenticacion;

    // FIX CICLO DE DEPENDENCIAS CON @Lazy
    public FiltroSolicitudJwt(UtilidadJwt utilidadJwt, @Lazy ServicioAutenticacion servicioAutenticacion) {
        this.utilidadJwt = utilidadJwt;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ================================
        // 1. Ignorar rutas públicas (login/registro)
        // ================================
        if (request.getRequestURI().contains("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String headerAuth = request.getHeader("Authorization");
        String username = null;
        String tokenJwt = null;

        // ================================
        // 2. Extraer token Bearer
        // ================================
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            tokenJwt = headerAuth.substring(7);

            try {
                username = utilidadJwt.extraerUsername(tokenJwt);
            } catch (Exception e) {
                System.out.println("⚠️ Token inválido o corrupto");
            }
        }

        // ================================
        // 3. Validar token y autenticar usuario
        // ================================
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails detallesUsuario = servicioAutenticacion.loadUserByUsername(username);

            if (utilidadJwt.validarToken(tokenJwt, detallesUsuario)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                detallesUsuario,
                                null,
                                detallesUsuario.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Registrar autenticación en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ================================
        // 4. Continuar la cadena de filtros
        // ================================
        filterChain.doFilter(request, response);
    }
}
