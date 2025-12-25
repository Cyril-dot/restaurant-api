package com.resturant.Restaurant_Application.customer.security;

import com.resturant.Restaurant_Application.customer.entity.CustomerEntity;
import com.resturant.Restaurant_Application.restaurant.admin.AdminEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminRefreshTokenRepo adminRefreshTokenRepository;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        AdminRefreshTokenRepo adminRefreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.adminRefreshTokenRepository = adminRefreshTokenRepository;
    }

    // ===================== SIGNING KEY =====================
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ===================== ACCESS TOKEN =====================
    public String generateAccessToken(CustomerEntity user) {
        return generateAccessToken(user.getEmail(), user.getRole().name());
    }

    public String generateAccessTokenForAdmin(AdminEntity admin) {
        return generateAccessToken(admin.getEmail(), admin.getRole().name());
    }

    private String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ===================== REFRESH TOKENS =====================
    public RefreshToken generateRefreshToken(CustomerEntity user) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByCustomer(user);

        RefreshToken token = existing.orElseGet(() -> RefreshToken.builder()
                .customer(user)
                .build());

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        System.out.println((existing.isPresent() ? "Updated" : "Created") +
                " customer refresh token: " + token.getToken());

        return refreshTokenRepository.save(token);
    }

    public AdminRefreshToken generateRefreshTokenForAdmin(AdminEntity admin) {
        Optional<AdminRefreshToken> existing = adminRefreshTokenRepository.findByAdmin(admin);

        AdminRefreshToken token = existing.orElseGet(() -> AdminRefreshToken.builder()
                .admin(admin)
                .build());

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        System.out.println((existing.isPresent() ? "Updated" : "Created") +
                " admin refresh token: " + token.getToken());

        return adminRefreshTokenRepository.save(token);
    }

    // ===================== VALIDATE TOKEN =====================
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getEmailFromAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException ex) {
            throw new RuntimeException("Invalid access token");
        }
    }

    public String getRoleFromAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
        } catch (JwtException ex) {
            throw new RuntimeException("Invalid access token");
        }
    }

    // ===================== TOKEN PAIR DTO =====================
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
