package com.quantumleap.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${quantumleap.jwt.secret:defaultSecretKey}")
    private String secretKey;

    @Value("${quantumleap.jwt.expiration:86400000}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        // Ensure the secret key is at least 32 characters (256 bits) for security
        if (secretKey.length() < 32) {
            // Pad the secret key to 32 characters if it's too short
            secretKey = secretKey + "0".repeat(32 - secretKey.length());
        }
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String userId = claims.getSubject();
        return UUID.fromString(userId);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.get("username", String.class);
    }

    public long getExpiration() {
        return expiration;
    }
}
