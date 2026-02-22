package com.example.chat.auth.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);

    void deleteByTokenValue(String tokenValue);

    void deleteByPrincipalId(UUID principalId);
}
