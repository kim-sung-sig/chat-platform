package com.example.chat.auth.server.core.repository;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;

import java.util.Optional;
import java.util.UUID;

/**
 * Credential 저장소
 */
public interface CredentialRepository {
    Optional<Credential> findByPrincipalId(UUID principalId, CredentialType credentialType);

    void save(UUID principalId, Credential credential);

    void delete(UUID principalId, CredentialType credentialType);

    boolean hasCredential(UUID principalId, CredentialType credentialType);
}
