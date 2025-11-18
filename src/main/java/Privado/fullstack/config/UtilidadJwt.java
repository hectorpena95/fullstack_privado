package Privado.fullstack.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class UtilidadJwt {

    // Inyectado desde application.properties
    @Value("${app.jwt.secret}")
    private String secret;

    // Inyectado desde application.properties
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpiration;

    // --- MÉTODOS PÚBLICOS CRUCIALES ---

    /**
     * 1. Extrae el nombre de usuario (subject) del token JWT.
     */
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * 2. Genera un token JWT para un usuario.
     */
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return crearToken(claims, userDetails.getUsername());
    }

    /**
     * 3. Valida si el token es válido para el usuario y no ha expirado.
     */
    public Boolean validarToken(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername()) && !esTokenExpirado(token));
    }

    // --- MÉTODOS AUXILIARES PRIVADOS ---

    private String crearToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private Boolean esTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Claims extraerTodasLasClaims(String token) {
        // Crear un JwtParser utilizando el builder
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Configurar la clave de firma
                .build();  // Obtener el JwtParser

        // Usar el JwtParser para parsear el JWT
        return parser.parseClaimsJws(token).getBody();  // Parsear el token y obtener el cuerpo de los claims
    }


    // Método auxiliar para extraer claims específicas
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }

    // Método para generar la clave de firma segura a partir de la cadena secreta
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);  // Decodificación de la clave secreta
        return Keys.hmacShaKeyFor(keyBytes);  // Generación de la clave de firma
    }
}
