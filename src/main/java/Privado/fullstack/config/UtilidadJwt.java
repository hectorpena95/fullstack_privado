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

    // ===============================================================
    // EXTRAER USERNAME
    // ===============================================================
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // ===============================================================
    // VALIDAR TOKEN (CORREGIDO)
    // Antes solo revisaba la fecha → FALTABA validar firma e integridad
    // ===============================================================
    public Boolean validarToken(String token) {
        try {
            extraerTodasLasClaims(token); // valida firma, expiración e integridad
            return true;
        } catch (Exception e) {
            System.out.println("❌ Token inválido o manipulado: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // GENERAR TOKEN
    // ===============================================================
    public String generarToken(org.springframework.security.core.userdetails.UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority()) // ROLE_ADMIN
                .toList()
        );

        return crearToken(claims, userDetails.getUsername());
    }

    // ===============================================================
    // FECHA DE EXPIRACIÓN DEL TOKEN
    // ===============================================================
    private Boolean esTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    // ===============================================================
    // CREAR TOKEN
    // ===============================================================
    private String crearToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ===============================================================
    // EXTRAER CLAIMS (validando firma)
    // ===============================================================
    private Claims extraerTodasLasClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();

        return parser.parseClaimsJws(token).getBody();
    }

    // ===============================================================
    // EXTRAER CLAIM GENÉRICO
    // ===============================================================
    public <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerTodasLasClaims(token));
    }

    // ===============================================================
    // LLAVE DE FIRMA
    // ===============================================================
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
