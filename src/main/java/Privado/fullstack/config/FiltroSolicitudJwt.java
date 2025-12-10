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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        System.out.println("➡️ URI recibida: " + request.getRequestURI());

        // 👉 Evita bloquear OPTIONS (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 👉 Saltar login
        if (request.getRequestURI().contains("/api/v1/auth/")) {
            System.out.println("⛔ Saltando filtro por /auth");
            filterChain.doFilter(request, response);
            return;
        }

        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);

        String username;
        try {
            username = utilidadJwt.extraerUsername(token);
        } catch (Exception e) {
            System.out.println("⚠️ Token inválido: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (!utilidadJwt.validarToken(token)) {
                System.out.println("❌ Token rechazado (firma o expiración)");
                filterChain.doFilter(request, response);
                return;
            }

            // 🔥 Cargar usuario real
            var userDetails = servicioAutenticacion.loadUserByUsername(username);

            List<String> listaRoles = utilidadJwt.extraerClaim(token, claims -> {
                Object raw = claims.get("authorities");
                if (raw instanceof List<?> list) {
                    return list.stream()
                            .map(Object::toString)
                            .toList();
                }
                return List.of(); // si está malo o null, evitamos excepción
            });


            var authorities = listaRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            System.out.println("🔎 Authorities usando token: " + authorities);

            // 🔥 Token de autenticación correcto
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,   // ✔️ UserDetails real
                            null,
                            authorities
                    );

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
