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

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpiration;

    // =====================================
    // EXTRAER USERNAME (SUB DE JWT)
    // =====================================
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // =====================================
    // 🔥 GENERAR TOKEN *CON ROLES*
    // =====================================
    public String generarToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        // AGREGAR ROLES
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())   // "ROLE_ADMIN" → "ADMIN"
                .toList()
        );

        return crearToken(claims, userDetails.getUsername());
    }

    // =====================================
    // VALIDAR TOKEN
    // =====================================
    public Boolean validarToken(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername()) && !esTokenExpirado(token));
    }

    // =====================================
    // MÉTODOS PRIVADOS
    // =====================================
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
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();

        return parser.parseClaimsJws(token).getBody();
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
