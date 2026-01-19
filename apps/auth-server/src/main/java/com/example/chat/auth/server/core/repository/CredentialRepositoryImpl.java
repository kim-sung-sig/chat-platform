package com.example.chat.auth.server.core.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.example.chat.auth.server.core.domain.Credential;

/**
 * Credential 임시 저장소 (In-Memory)
 * - 실제 구현에서는 DB 사용
 * - 현재는 개발 편의상 메모리
 */
@Repository
public class CredentialRepositoryImpl implements CredentialRepository {

    private final Map<String, Credential> storage = new HashMap<>();

    @Override
    public Optional<Credential> findByPrincipalId(UUID principalId, String credentialType) {
        String key = principalId + ":" + credentialType;
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public void save(UUID principalId, Credential credential) {
        String key = principalId + ":" + credential.getType().name();
        storage.put(key, credential);
    }

    @Override
    public void delete(UUID principalId, String credentialType) {
        String key = principalId + ":" + credentialType;
        storage.remove(key);
    }

    @Override
    public boolean hasCredential(UUID principalId, String credentialType) {
        String key = principalId + ":" + credentialType;
        return storage.containsKey(key);
    }
}
