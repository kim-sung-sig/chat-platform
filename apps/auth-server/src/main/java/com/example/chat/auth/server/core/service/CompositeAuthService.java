package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.CredentialType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 복합 인증 서비스
 */
@Service
public class CompositeAuthService {

    /**
     * 여러 자격증명 결과를 조합하여 최종 인증 수준 결정
     */
    public AuthResult combineResults(AuthResult first, AuthResult second) {
        if (!first.authenticated() || !second.authenticated()) {
            return AuthResult.failure("One or more authentication methods failed");
        }

        Set<CredentialType> combined = new HashSet<>(first.completedCredentials());
        combined.addAll(second.completedCredentials());

        AuthLevel finalLevel = first.authLevel().getLevel() > second.authLevel().getLevel()
                ? first.authLevel()
                : second.authLevel();

        return AuthResult.success(finalLevel, combined);
    }

    /**
     * 인증 수준 업그레이드
     */
    public AuthLevel upgradeAuthLevel(AuthLevel current, AuthResult additional) {
        if (additional.authLevel() != null && additional.authLevel().getLevel() > current.getLevel()) {
            return additional.authLevel();
        }
        return current;
    }
}
