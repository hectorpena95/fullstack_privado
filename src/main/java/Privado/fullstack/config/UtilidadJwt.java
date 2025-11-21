package Privado.fullstack.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class UtilidadJwt {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpiration;

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // ✅ CORREGIDO: ahora no depende de UserDetails
    public Boolean validarToken(String token) {
        return !esTokenExpirado(token);
    }

    // Generar token con roles sin prefijo ROLE_
    public String generarToken(org.springframework.security.core.userdetails.UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", "")) // ADMIN
                .toList()
        );

        return crearToken(claims, userDetails.getUsername());
    }

    private Boolean esTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private String crearToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extraerTodasLasClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();

        return parser.parseClaimsJws(token).getBody();
    }

    public <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerTodasLasClaims(token));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
