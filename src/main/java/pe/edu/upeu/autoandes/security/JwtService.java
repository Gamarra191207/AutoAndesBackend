package pe.edu.upeu.autoandes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret:w9Z6dP/dzykNH/uiI3Z/KGjBlP0H4Ji+Tuq4fm2cTNuLD4jWafu1gBL74V2SJEvy}")
    private String jwtSecret;

    @Value("${app.jwt.expiracion-segundos:3600}")
    private long expiracionSegundos;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(String username, String nombrecompleto, List<String> roles, List<String> permisos) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + (expiracionSegundos * 1000));

        return Jwts.builder()
                .subject(username)
                .claim("nombrecompleto", nombrecompleto)
                .claim("roles", roles)
                .claim("authorities", permisos)
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean esTokenValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Firma o formato de token JWT invalido: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Error validando token JWT: {}", e.getMessage());
        }
        return false;
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerAuthorities(String token) {
        Object authClaim = extraerClaims(token).get("authorities");
        if (authClaim instanceof List<?>) {
            return (List<String>) authClaim;
        }
        return Collections.emptyList();
    }

    public long getExpiracionSegundos() {
        return expiracionSegundos;
    }
}
