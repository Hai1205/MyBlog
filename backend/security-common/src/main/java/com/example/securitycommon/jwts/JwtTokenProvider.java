package com.example.securitycommon.jwts;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.securitycommon.exceptions.JwtValidationException;
import com.example.securitycommon.models.AuthenticatedUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";

    @Value("${JWT_PUBLIC_KEY}")
    private String publicKeyStr;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String cleanedKey = publicKeyStr.replaceAll("\\s", "");
            byte[] decodedKey = Base64.getDecoder().decode(cleanedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception ex) {
            throw new JwtValidationException("Unable to load JWT public key", ex);
        }
    }

    public AuthenticatedUser validateAndExtract(String token) {
        Claims claims = parseClaims(token);

        String tokenType = claims.get("tokenType", String.class);
        if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
            throw new JwtValidationException("Unsupported token type");
        }

        String role = claims.get("role", String.class);
        String email = claims.get("email", String.class);
        String username = claims.get("username", String.class);
        String userId = claims.get("userId", String.class);

        if (role == null || userId == null || (email == null && username == null)) {
            throw new JwtValidationException("Missing required token claims");
        }

        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
            throw new JwtValidationException("Token has expired");
        }

        try {
            String principalEmail = email != null ? email : username;
            return new AuthenticatedUser(UUID.fromString(userId), principalEmail, role);
        } catch (IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid user id in token", ex);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid token", ex);
        }
    }
}
