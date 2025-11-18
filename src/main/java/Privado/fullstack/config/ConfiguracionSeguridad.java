
package Privado.fullstack.config;

import Privado.fullstack.service.ServicioAutenticacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    // 2. CORRECCIÓN: Inyectamos ServicioAutenticacion
    private final ServicioAutenticacion servicioAutenticacion;

    public ConfiguracionSeguridad(ServicioAutenticacion servicioAutenticacion) { // 3. CORRECCIÓN: Constructor
        this.servicioAutenticacion = servicioAutenticacion;
    }

    // Bean para el encriptador de contraseñas (BCrypt)
    @Bean
    public PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    // Bean para el manejador de autenticación
    @Bean
    public AuthenticationManager administradorAutenticacion(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Bean principal que configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain cadenaFiltrosSeguridad(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF
                .csrf(csrf -> csrf.disable())

                // Configura la gestión de sesiones: sin estado (STATELESS) para JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura las reglas de autorización de peticiones (rutas)
                .authorizeHttpRequests(auth -> auth

                        // 1. Rutas públicas (Catálogo y Autenticación)
                        .requestMatchers("/api/v1/auth/**").permitAll() // Login y Registro
                        .requestMatchers("/api/v1/productos/**").permitAll() // Catálogo público (lectura)

                        // 2. Rutas de Cliente (ej: crear pedido, ver sus pedidos)
                        // NOTA: Debes cambiar la regla aquí si quieres que el CLIENTE pueda ver sus pedidos.
                        // Para pruebas iniciales, lo dejamos así.
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ADMIN", "VENDOR", "CLIENT")

                        // 3. Rutas de Administración (Solo ADMIN)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 4. Las demás rutas deben ser autenticadas
                        .anyRequest().authenticated()
                );

        // NOTA: Falta añadir el FiltroSolicitudJwt aquí.

        return http.build();
    }
}