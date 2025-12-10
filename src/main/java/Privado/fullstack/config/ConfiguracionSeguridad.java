package Privado.fullstack.config;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class ConfiguracionSeguridad {

    @Autowired
    private FiltroSolicitudJwt filtroSolicitudJwt;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("Authorization"));
                    config.setAllowCredentials(false); // ← IMPORTANTE PARA SWAGGER
                    return config;
                }))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // permitir preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"                   // ← AGREGADO
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDEDOR")

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
