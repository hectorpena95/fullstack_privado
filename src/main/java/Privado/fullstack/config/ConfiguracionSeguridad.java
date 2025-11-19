package Privado.fullstack.config;

import Privado.fullstack.service.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Autowired; // Necesario para @Lazy en constructor
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy; // Importación necesaria
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final ServicioAutenticacion servicioAutenticacion;
    private final FiltroSolicitudJwt filtroSolicitudJwt;

    // 🔑 FIX FINAL DEL CICLO: Ambos beans en el constructor son marcados como @Lazy.
    // Esto es necesario porque el servicio es requerido tanto por el AuthenticationManager como por el constructor.
    @Autowired
    public ConfiguracionSeguridad(@Lazy ServicioAutenticacion servicioAutenticacion, @Lazy FiltroSolicitudJwt filtroSolicitudJwt) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.filtroSolicitudJwt = filtroSolicitudJwt;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Define el AuthenticationManager usando el ServicioAutenticacion limpio
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Aquí se usa el bean servicioAutenticacion, el cual es inyectado de forma @Lazy
        authProvider.setUserDetailsService(servicioAutenticacion);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    // =================================================================
    // FIX CLAVE 403: WebSecurityCustomizer ignora COMPLETAMENTE las rutas /api/v1/auth/**
    // =================================================================
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // Ignorar completamente el filtro de seguridad para las rutas de autenticación (Login/Registro)
                .requestMatchers("/api/v1/auth/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desactivar CSRF

                .authorizeHttpRequests(auth -> auth
                        // Permite solicitudes OPTIONS (necesario para el CORS Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Protege todas las demás rutas
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Uso de JWT
                );

        // Registro del Filtro JWT (se ejecutará SOLAMENTE para rutas protegidas, no para /auth/**)
        http.addFilterBefore(filtroSolicitudJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean de CorsFilter para permitir el origen 5173 (Frontend React/Vite)
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}