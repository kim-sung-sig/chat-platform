package com.example.chat.auth.server.core.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.CredentialType;

/**
 * 복합 인증 서비스
 * - 여러 자격증명을 조합하여 인증 수준 결정
 * - LOW + OTP = MEDIUM
 * - Passkey = HIGH
 */
@Service
public class CompositeAuthService {

    /**
     * 여러 자격증명 결과를 조합하여 최종 인증 수준 결정
     */
    public AuthResult combineResults(AuthResult first, AuthResult second) {
        // 하나라도 실패하면 전체 실패
        if (!first.isAuthenticated() || !second.isAuthenticated()) {
            return AuthResult.failure("One or more authentication methods failed");
        }

        // 완료된 자격증명 합치기
        Set<CredentialType> combined = new HashSet<>(first.getCompletedCredentials());
        combined.addAll(second.getCompletedCredentials());

        // 인증 수준 결정: 높은 쪽을 선택
        AuthLevel finalLevel = first.getAuthLevel().getLevel() > second.getAuthLevel().getLevel()
                ? first.getAuthLevel()
                : second.getAuthLevel();

        return AuthResult.success(finalLevel, combined);
    }

    /**
     * 암호화된 인증 수준 업그레이드
     * 예: LOW + OTP = MEDIUM
     */
    public AuthLevel upgradeAuthLevel(AuthLevel current, AuthResult additional) {
        if (additional.getAuthLevel().getLevel() > current.getLevel()) {
            return additional.getAuthLevel();
        }
        return current;
    }
}
