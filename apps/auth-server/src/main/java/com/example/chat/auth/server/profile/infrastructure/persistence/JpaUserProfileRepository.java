package com.example.chat.auth.server.profile.infrastructure.persistence;

import com.example.chat.auth.server.profile.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByPrincipalId(UUID principalId);
    boolean existsByPrincipalId(UUID principalId);
}
