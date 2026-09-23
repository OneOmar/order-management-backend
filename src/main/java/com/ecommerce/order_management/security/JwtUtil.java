package com.ecommerce.order_management.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

/**
 * Utilitaire simple pour générer / valider les JWT (HS256).
 * - secret et expiration sont fournis via application.properties ou variables d'env.
 * - méthodes principales : generateToken, extractUsername, isTokenValid
 * Remarques concises :
 * - jwt.secret doit être une chaîne suffisamment longue (>= 32 bytes pour HS256).
 * - Ne pas stocker le secret en clair dans le dépôt pour la prod.
 */
@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("jwt.secret must be provided and at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un token HS256 compact pour l'utilisateur.
     * Contient le username en subject; ajouter des claims si besoin.
     */
    public String generateToken(UserDetails userDetails) {
        Date now = Date.from(Instant.now());
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrait le username (subject) depuis le token. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valide le token par rapport à l'utilisateur donné.
     * Retourne false en cas de token invalide / expiré / mauvaise signature.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username != null
                    && username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            // token invalide / signature invalide / format incorrect
            return false;
        }
    }

    /** Vérifie si le token est expiré. */
    private boolean isTokenExpired(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.before(Date.from(Instant.now()));
    }

    /**
     * Parse et retourne les Claims du token.
     * Utilise l'API moderne parserBuilder() (assure-toi que tes dépendances jjwt sont alignées).
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
