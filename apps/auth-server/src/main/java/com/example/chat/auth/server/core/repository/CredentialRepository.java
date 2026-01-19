package com.example.chat.auth.server.core.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.chat.auth.server.core.domain.Credential;

/**
 * Credential 저장소
 * - 어떤 주체가 어떤 자격증명을 가지고 있는지 추적
 */
public interface CredentialRepository {

    /**
     * 주체의 자격증명 조회
     */
    Optional<Credential> findByPrincipalId(UUID principalId, String credentialType);
    /**
     * 자격증명 저장
     */
    void save(UUID principalId, Credential credential);

    /**
     * 자격증명 삭제
     */
    void delete(UUID principalId, String credentialType);

    /**
     * 주체가 특정 자격증명 타입을 가지고 있는가?
     */
    boolean hasCredential(UUID principalId, String credentialType);
}
