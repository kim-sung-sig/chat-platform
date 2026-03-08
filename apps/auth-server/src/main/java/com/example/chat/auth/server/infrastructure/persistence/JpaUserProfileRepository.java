package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.profile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByPrincipalId(UUID principalId);
    boolean existsByPrincipalId(UUID principalId);
}
