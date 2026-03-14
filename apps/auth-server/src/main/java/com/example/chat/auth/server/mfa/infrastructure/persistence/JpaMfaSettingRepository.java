package com.example.chat.auth.server.mfa.infrastructure.persistence;

import com.example.chat.auth.server.mfa.domain.MfaType;
import com.example.chat.auth.server.mfa.domain.mfa.MfaSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMfaSettingRepository extends JpaRepository<MfaSetting, UUID> {
    List<MfaSetting> findAllByPrincipalId(UUID principalId);

    Optional<MfaSetting> findByPrincipalIdAndMfaType(UUID principalId, MfaType mfaType);

    boolean existsByPrincipalIdAndMfaTypeAndEnabled(UUID principalId, MfaType mfaType, boolean enabled);
}
