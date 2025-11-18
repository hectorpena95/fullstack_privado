package Privado.fullstack.config;


import Privado.fullstack.service.ServicioDetallesUsuario; // Usamos el nombre en español
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
@EnableWebSecurity // Habilita la seguridad web
@EnableMethodSecurity // Permite usar @PreAuthorize en métodos
public class ConfiguracionSeguridad {

    // Inyectamos el servicio de detalles de usuario
    private final ServicioDetallesUsuario servicioDetallesUsuario;

    public ConfiguracionSeguridad(ServicioDetallesUsuario servicioDetallesUsuario) {
        this.servicioDetallesUsuario = servicioDetallesUsuario;
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
                // Deshabilita CSRF (típico para APIs REST sin sesiones basadas en cookies)
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

                        // 2. Rutas de Vendedor (ADMIN y VENDOR)
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ADMIN", "VENDOR") // Ver pedidos

                        // 3. Rutas de Administración (Solo ADMIN)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // Rutas de gestión de usuarios/roles/stock

                        // 4. Las demás rutas deben ser autenticadas
                        .anyRequest().authenticated()
                );

        // NOTA: Más adelante, añadiremos el JwtRequestFilter aquí.

        return http.build();
    }
}