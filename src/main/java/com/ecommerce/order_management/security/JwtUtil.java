package com.ecommerce.order_management.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtil:
 * - génère access + refresh tokens (HS256),
 * - valide et extrait claims,
 * - clé configurée en Base64 via jwt.secret.
 */
@Component
public class JwtUtil {

    // Clé de signature HMAC
    private final SecretKey key;

    // Durées d'expiration (ms) injectées depuis application.properties
    private final long expirationMs;
    private final long refreshExpirationMs;

    // Constructor-based injection : initialise la clé à partir du secret Base64
    public JwtUtil(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.expiration}") long expirationMs,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /* ---------- Génération de tokens ---------- */

    // Access token (access)
    public String generateAccessToken(UserDetails user) {
        return buildToken(user.getUsername(), expirationMs, "access");
    }

    // Refresh token (refresh)
    public String generateRefreshToken(UserDetails user) {
        return buildToken(user.getUsername(), refreshExpirationMs, "refresh");
    }

    // Construction du token
    private String buildToken(String subject, long expiryMs, String type) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(subject)
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /* ---------- Parsing / extraction / validation (explicit) ---------- */

    // Extrait le username (subject)
    public String extractUsername(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // Extrait la date d'expiration — explicite
    public Date extractExpiration(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    // Extrait la claim custom "type" si présente (ex: "access" ou "refresh")
    public String extractType(String token) {
        Claims claims = parseClaims(token);
        Object type = claims.get("type");
        return type != null ? type.toString() : null;
    }

    // Vérifie si token valide pour l'utilisateur
    public boolean isTokenValid(String token, UserDetails user) {
        try {
            String username = extractUsername(token);
            return username != null
                    && username.equals(user.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // Vérifie si refresh token
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }

    // Vérifie si access token
    public boolean isAccessToken(String token) {
        return "access".equals(extractType(token));
    }

    // Vérifie l'expiration — explicite
    private boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp.before(new Date());
    }

    /* ---------- Parsing centralisé (méthode unique) ---------- */

    // supprime "Bearer "
    private String normalizeToken(String token) {
        if (token == null) return null;

        token = token.trim();

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    /**
     * Parse et retourne les Claims du token en utilisant la clé `key`.
     * Lance JwtException en cas de token invalide/expiré.
     */
    private Claims parseClaims(String token) {
        String actual = normalizeToken(token);
        if (actual == null || actual.isEmpty()) {
            throw new JwtException("Token vide ou null");
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(actual)
                .getBody();
    }
}
