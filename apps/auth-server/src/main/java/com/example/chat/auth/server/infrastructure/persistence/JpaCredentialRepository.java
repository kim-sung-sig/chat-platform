package com.example.chat.auth.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCredentialRepository extends JpaRepository<CredentialEntity, UUID> {
    Optional<CredentialEntity> findByPrincipalIdAndType(UUID principalId, String type);

    void deleteByPrincipalIdAndType(UUID principalId, String type);
}
