package com.example.chat.auth.server.core.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.chat.auth.server.core.domain.RefreshToken;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    void save(RefreshToken refreshToken);

    void deleteByTokenValue(String tokenValue);

    void deleteByPrincipalId(UUID principalId);

}
