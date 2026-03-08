package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.MfaType;
import com.example.chat.auth.server.core.domain.mfa.MfaSetting;
import com.example.chat.auth.server.core.repository.MfaSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MfaSettingRepositoryImpl implements MfaSettingRepository {

    private final JpaMfaSettingRepository jpaMfaSettingRepository;

    @Override
    public List<MfaSetting> findAllByPrincipalId(UUID principalId) {
        return jpaMfaSettingRepository.findAllByPrincipalId(principalId);
    }

    @Override
    public Optional<MfaSetting> findByPrincipalIdAndType(UUID principalId, MfaType mfaType) {
        return jpaMfaSettingRepository.findByPrincipalIdAndMfaType(principalId, mfaType);
    }

    @Override
    public MfaSetting save(MfaSetting mfaSetting) {
        return jpaMfaSettingRepository.save(mfaSetting);
    }

    @Override
    public void delete(MfaSetting mfaSetting) {
        jpaMfaSettingRepository.delete(mfaSetting);
    }

    @Override
    public boolean existsByPrincipalIdAndTypeAndEnabled(UUID principalId, MfaType mfaType, boolean enabled) {
        return jpaMfaSettingRepository.existsByPrincipalIdAndMfaTypeAndEnabled(principalId, mfaType, enabled);
    }
}
