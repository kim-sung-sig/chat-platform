package com.example.chat.auth.server.core.repository;

import com.example.chat.auth.server.core.domain.profile.UserProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * UserProfile 저장소 인터페이스
 */
public interface UserProfileRepository {
    Optional<UserProfile> findByPrincipalId(UUID principalId);

    UserProfile save(UserProfile profile);

    boolean existsByPrincipalId(UUID principalId);
}
