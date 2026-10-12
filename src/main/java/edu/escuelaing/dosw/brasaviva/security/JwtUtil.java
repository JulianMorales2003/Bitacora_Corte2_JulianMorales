package edu.escuelaing.dosw.brasaviva.security;

import edu.escuelaing.dosw.brasaviva.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/** Genera y valida tokens JWT firmados con HMAC-SHA. */
@Component
public class JwtUtil {

    static final String CLAIM_ROL = "rol";

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(JwtProperties properties) {
        // Si el secreto es menor a 256 bits, jjwt lanza WeakKeyException y la app no arranca (fail-fast)
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(String email, String rol) {
        return generateToken(email, rol, expirationMs);
    }

    /** Variante con vigencia propia; permite generar tokens ya vencidos en las pruebas. */
    public String generateToken(String email, String rol, long ttlMs) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROL, rol)
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + ttlMs))
                .signWith(key)
                .compact();
    }

    /** Verifica la firma y la expiracion; lanza {@link JwtException} si el token no es valido. */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
