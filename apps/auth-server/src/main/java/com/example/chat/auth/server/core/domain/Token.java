package com.example.chat.auth.server.core.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JWT 토큰 정보
 * - 인증 성공 후 발급되는 토큰
 * - AuthLevel과 Principal 정보를 담음
 */
public class Token {

    private final String accessToken;
    private final String refreshToken;
    private final Instant expiresAt;
    private final Instant refreshExpiresAt;
    private final UUID principalId;
    private final AuthLevel authLevel;
    private final TokenType tokenType;

    public Token(String accessToken, String refreshToken,
                Instant expiresAt, Instant refreshExpiresAt,
                UUID principalId, AuthLevel authLevel,
                TokenType tokenType) {
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken cannot be null");
        this.refreshToken = refreshToken;  // MFA 토큰은 refresh 없을 수도
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.refreshExpiresAt = refreshExpiresAt;
        this.principalId = Objects.requireNonNull(principalId, "principalId cannot be null");
        this.authLevel = Objects.requireNonNull(authLevel, "authLevel cannot be null");
        this.tokenType = Objects.requireNonNull(tokenType, "tokenType cannot be null");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public AuthLevel getAuthLevel() {
        return authLevel;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", authLevel=" + authLevel +
                ", principalId=" + principalId +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
