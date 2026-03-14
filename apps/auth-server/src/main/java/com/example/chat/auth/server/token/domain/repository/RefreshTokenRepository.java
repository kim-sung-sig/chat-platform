package com.example.chat.auth.server.token.domain.repository;

import com.example.chat.auth.server.token.domain.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void save(RefreshToken refreshToken);

    void deleteByTokenValue(String tokenValue);

    void deleteByPrincipalId(UUID principalId);
}
