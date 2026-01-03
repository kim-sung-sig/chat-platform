package com.example.chat.modules.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity-in-seconds:604800}") long refreshTokenValidity
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity * 1000;
        this.refreshTokenValidity = refreshTokenValidity * 1000;
    }

    // Generate Access Token
    public String createAccessToken(String userId, String role) {
        return createToken(userId, role, accessTokenValidity);
    }

    // Generate Refresh Token
    public String createRefreshToken(String userId) {
        return createToken(userId, null, refreshTokenValidity);
    }

    private String createToken(String subject, String role, long validity) {
        Date now = new Date();
        Date validityDate = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(validityDate)
                .signWith(key);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // Validate Token and return Claims (throws exception if invalid)
    public Jws<Claims> validateAndParseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public String getUserId(String token) {
        return validateAndParseToken(token).getPayload().getSubject();
    }
}
