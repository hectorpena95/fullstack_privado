package Privado.fullstack.config;

import Privado.fullstack.service.ServicioAutenticacion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FiltroSolicitudJwt extends OncePerRequestFilter {

    private final UtilidadJwt utilidadJwt;
    private final ServicioAutenticacion servicioAutenticacion;

    public FiltroSolicitudJwt(UtilidadJwt utilidadJwt, @Lazy ServicioAutenticacion servicioAutenticacion) {
        this.utilidadJwt = utilidadJwt;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().contains("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String headerAuth = request.getHeader("Authorization");
        String username = null;
        String tokenJwt = null;

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            tokenJwt = headerAuth.substring(7);

            try {
                username = utilidadJwt.extraerUsername(tokenJwt);
            } catch (Exception e) {
                System.out.println("⚠️ Token inválido");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = servicioAutenticacion.loadUserByUsername(username);

            if (utilidadJwt.validarToken(tokenJwt, userDetails)) {

                // 👉 EXTRAER ROLES DEL TOKEN
                List<String> roles = utilidadJwt.extraerClaim(tokenJwt, claims ->
                        (List<String>) claims.get("roles")
                );

                // Convertirlos a SimpleGrantedAuthority
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
