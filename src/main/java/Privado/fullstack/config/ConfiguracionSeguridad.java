package Privado.fullstack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy; // 💡 Importante: Añadir la importación de @Lazy
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    private final FiltroSolicitudJwt filtroSolicitudJwt;

    // 🔑 CORRECCIÓN FINAL: Usamos @Lazy aquí para que el filtro se inicialice
    // solo cuando sea estrictamente necesario, rompiendo el ciclo.
    public ConfiguracionSeguridad(@Lazy FiltroSolicitudJwt filtroSolicitudJwt) {
        this.filtroSolicitudJwt = filtroSolicitudJwt;
    }

    // --- Beans Fundamentales ---

    @Bean
    public PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    // El AuthenticationManager se inyecta correctamente en ServicioAutenticacion
    // cuando se define aquí (y ServicioAutenticacion tiene @Lazy).
    @Bean
    public AuthenticationManager administradorAutenticacion(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // --- Cadena de Filtros de Seguridad ---

    @Bean
    public SecurityFilterChain cadenaFiltrosSeguridad(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF
                .csrf(csrf -> csrf.disable())

                // Configura la gestión de sesiones: sin estado (STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura las reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/productos/**").permitAll()

                        // Rutas protegidas
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ADMIN", "VENDOR", "CLIENT")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );

        // ✅ Añadir el FiltroSolicitudJwt a la cadena
        http.addFilterBefore(filtroSolicitudJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}