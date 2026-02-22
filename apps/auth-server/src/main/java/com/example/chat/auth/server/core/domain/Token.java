package com.example.chat.auth.server.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * JWT 토큰 정보
 */
public record Token(
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        Instant refreshExpiresAt,
        UUID principalId,
        AuthLevel authLevel,
        TokenType tokenType) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
