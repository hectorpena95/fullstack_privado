package Privado.fullstack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class ConfiguracionSeguridad {

    private final FiltroSolicitudJwt filtroSolicitudJwt;

    public ConfiguracionSeguridad(FiltroSolicitudJwt filtroSolicitudJwt) {
        this.filtroSolicitudJwt = filtroSolicitudJwt;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // ===============================
        // 🌍 CONFIGURACIÓN CORS GLOBAL
        // ===============================
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration cors = new CorsConfiguration();

        // ❗ Permite acceso desde toda red LAN / Celulares / Web
        cors.addAllowedOriginPattern("*");

        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));

        // ❗ No usar credenciales con "*"
        cors.setAllowCredentials(false);

        source.registerCorsConfiguration("/**", cors);

        http
                .cors(c -> c.configurationSource(source))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // OPTIONS siempre permitido
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ============================
                        // 🟢 ENDPOINTS PÚBLICOS
                        // ============================
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()

                        // Productos → GET público
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**")
                        .permitAll()

                        // ============================
                        // 🔐 ENDPOINTS PRIVADOS
                        // ============================
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

                        // Todo lo demás requiere login
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(filtroSolicitudJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
