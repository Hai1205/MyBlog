package com.example.authservice.services;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${JWT_PRIVATE_KEY}")
    private String privateKeyStr;
    private PrivateKey privateKey;

    @Value("${JWT_PUBLIC_KEY}")
    private String publicKeyStr;
    private PublicKey publicKey;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 5; // 5 hours

    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    @PostConstruct
    public void init() {
        this.privateKey = (PrivateKey) getKey(privateKeyStr, true);
        this.publicKey = (PublicKey) getKey(publicKeyStr, false);
    }

    private Key getKey(String key, boolean isPrivate) {
        try {
            String cleanedKey = key.replaceAll("\\s", "");

            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(cleanedKey);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Không thể giải mã Base64 cho " +
                        (isPrivate ? "khóa riêng tư" : "khóa công khai") +
                        ". Đảm bảo rằng khóa ở định dạng Base64 hợp lệ.", e);
            }

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            try {
                if (isPrivate) {
                    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
                    return keyFactory.generatePrivate(keySpec);
                } else {
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
                    return keyFactory.generatePublic(keySpec);
                }
            } catch (Exception e) {
                throw new RuntimeException("Không thể tạo " +
                        (isPrivate ? "khóa riêng tư" : "khóa công khai") +
                        " từ dữ liệu được cung cấp. Đảm bảo khóa có định dạng chính xác " +
                        (isPrivate ? "PKCS8" : "X509") + ".", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể tải " +
                    (isPrivate ? "khóa riêng tư" : "khóa công khai") +
                    ". Chi tiết: " + e.getMessage(), e);
        }
    }

    public String generateAccessToken(String userId, String email, String role, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        if (username != null) {
            claims.put("username", username);
        }
        claims.put("role", role);
        claims.put("tokenType", "ACCESS");

        String subject = email != null ? email : username;

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(privateKey)
                .compact();
    }

    public String generateAccessToken(String userId, String username, String role) {
        return generateAccessToken(userId, username, role, username);
    }

    public String generateRefreshToken(String userId, String email, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        if (username != null) {
            claims.put("username", username);
        }
        claims.put("tokenType", "REFRESH");

        String subject = email != null ? email : username;

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(privateKey)
                .compact();
    }

    public String generateRefreshToken(String userId, String username) {
        return generateRefreshToken(userId, username, username);
    }

    @Deprecated
    public String generateToken(String userId, String username, String role) {
        return generateAccessToken(userId, username, role, username);
    }

    public String extractUsername(String token) {
        return extractClaims(token, claims -> {
            String usernameClaim = claims.get("username", String.class);
            if (usernameClaim != null) {
                return usernameClaim;
            }
            return claims.getSubject();
        });
    }

    public String extractUserId(String token) {
        return extractClaims(token, claims -> claims.get("userId", String.class));
    }

    public String extractRole(String token) {
        return extractClaims(token, claims -> claims.get("role", String.class));
    }

    public String extractEmail(String token) {
        return extractClaims(token, claims -> {
            String emailClaim = claims.get("email", String.class);
            if (emailClaim != null) {
                return emailClaim;
            }
            return claims.getSubject();
        });
    }

    public String extractTokenType(String token) {
        return extractClaims(token, claims -> claims.get("tokenType", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload());
    }

    private Boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return false;
            }

            // Parse token to validate signature
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            // Check token type must be REFRESH
            String tokenType = claims.get("tokenType", String.class);
            if (!"REFRESH".equals(tokenType)) {
                return false;
            }

            // Check token has not expired
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return false;
            }

            boolean isExpired = expiration.before(new Date());
            return !isExpired;

        } catch (ExpiredJwtException e) {
            // Token is expired
            return false;
        } catch (SignatureException e) {
            // Signature is invalid
            return false;
        } catch (MalformedJwtException e) {
            // Token is malformed
            return false;
        } catch (Exception e) {
            // Other errors
            return false;
        }
    }

    public Boolean validateAccessToken(String accessToken, String username) {
        try {
            String tokenType = extractTokenType(accessToken);
            // Check token type must be ACCESS
            if (!"ACCESS".equals(tokenType)) {
                return false;
            }
            // Check username and expiration
            return validateToken(accessToken, username);
        } catch (Exception e) {
            return false;
        }
    }
}