package com.example.chat.auth.server.auth.domain.repository;

import com.example.chat.auth.server.auth.domain.Credential;
import com.example.chat.auth.server.auth.domain.CredentialType;
import com.example.chat.auth.server.auth.infrastructure.persistence.CredentialEntity;
import com.example.chat.auth.server.auth.infrastructure.persistence.JpaCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CredentialRepositoryImpl implements CredentialRepository {

    private final JpaCredentialRepository jpaRepository;

    @Override
    public Optional<Credential> findByPrincipalId(UUID principalId, CredentialType credentialType) {
        return jpaRepository.findByPrincipalIdAndType(principalId, credentialType.name())
                .map(CredentialEntity::toDomain);
    }

    @Override
    @Transactional
    public void save(UUID principalId, Credential credential) {
        Optional<CredentialEntity> existing = jpaRepository.findByPrincipalIdAndType(principalId,
                credential.getType().name());

        CredentialEntity entity = CredentialEntity.fromDomain(principalId, credential);

        existing.ifPresent(jpaRepository::delete);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(UUID principalId, CredentialType credentialType) {
        jpaRepository.deleteByPrincipalIdAndType(principalId, credentialType.name());
    }

    @Override
    public boolean hasCredential(UUID principalId, CredentialType credentialType) {
        return jpaRepository.findByPrincipalIdAndType(principalId, credentialType.name()).isPresent();
    }
}
