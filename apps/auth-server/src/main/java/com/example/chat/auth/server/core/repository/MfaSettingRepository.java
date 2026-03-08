package com.example.chat.auth.server.core.repository;

import com.example.chat.auth.server.core.domain.MfaType;
import com.example.chat.auth.server.core.domain.mfa.MfaSetting;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MfaSetting 저장소 인터페이스
 */
public interface MfaSettingRepository {
    List<MfaSetting> findAllByPrincipalId(UUID principalId);

    Optional<MfaSetting> findByPrincipalIdAndType(UUID principalId, MfaType mfaType);

    MfaSetting save(MfaSetting mfaSetting);

    void delete(MfaSetting mfaSetting);

    boolean existsByPrincipalIdAndTypeAndEnabled(UUID principalId, MfaType mfaType, boolean enabled);
}
