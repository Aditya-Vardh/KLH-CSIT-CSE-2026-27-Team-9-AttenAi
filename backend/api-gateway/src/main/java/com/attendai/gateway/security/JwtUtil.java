package com.attendai.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stateless JWT utility — parses and validates tokens without needing a database
 * or a round-trip to the auth-service. Uses the same secret / algorithm as JwtService
 * in auth-service.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${attendai.security.jwt-secret}") String secret) {
        // Mirror the key derivation in auth-service JwtService
        byte[] keyBytes = secret.length() >= 43
                ? secret.getBytes(StandardCharsets.UTF_8)
                : io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parses the token and returns its claims.
     *
     * @throws JwtException if the token is malformed, expired, or has a bad signature
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns true only if the token passes signature + expiry checks.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Extracts the subject (email) from a validated token. */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extracts the role claim as a single-element list. */
    public List<String> extractRoles(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? List.of() : List.of(String.valueOf(role));
    }

    /** Extracts the userId claim. */
    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Integer i) return i.longValue();
        if (userId instanceof Long l) return l;
        return null;
    }
}
