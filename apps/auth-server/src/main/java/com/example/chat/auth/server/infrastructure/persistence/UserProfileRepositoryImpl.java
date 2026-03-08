package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.profile.UserProfile;
import com.example.chat.auth.server.core.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final JpaUserProfileRepository jpaUserProfileRepository;

    @Override
    public Optional<UserProfile> findByPrincipalId(UUID principalId) {
        return jpaUserProfileRepository.findByPrincipalId(principalId);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return jpaUserProfileRepository.save(profile);
    }

    @Override
    public boolean existsByPrincipalId(UUID principalId) {
        return jpaUserProfileRepository.existsByPrincipalId(principalId);
    }
}
