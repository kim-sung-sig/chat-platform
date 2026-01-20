package com.example.chat.auth.server.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.auth.server.core.domain.RefreshToken;
import com.example.chat.auth.server.core.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRepository;

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return jpaRepository.findByTokenValue(tokenValue)
                .map(RefreshTokenEntity::toDomain);
    }

    @Override
    @Transactional
    public void save(RefreshToken refreshToken) {
        jpaRepository.save(RefreshTokenEntity.fromDomain(refreshToken));
    }

    @Override
    @Transactional
    public void deleteByTokenValue(String tokenValue) {
        jpaRepository.deleteByTokenValue(tokenValue);
    }

    @Override
    @Transactional
    public void deleteByPrincipalId(UUID principalId) {
        jpaRepository.deleteByPrincipalId(principalId);
    }
}
