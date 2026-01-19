package com.example.chat.auth.server.core.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;

/**
 * 소셜(OAuth) 인증 서비스
 * - OAuth 공급자(Google, Kakao, Apple 등)와 통신
 * - 토큰 검증
 * - 서비스가 "어떤 OAuth 공급자와 통신할 것인가" 결정
 */
@Service
public class SocialAuthService {

    /**
     * 소셜 인증 처리
     * 실제로는 OAuth 클라이언트 라이브러리를 사용하여 토큰 검증
     */
    public AuthResult authenticate(SocialCredential credential,
                                   AuthenticationContext context) {
        // 실제 구현에서는 OAuth 공급자에 토큰 검증
        // 예: Google OAuth API 호출
        if (!isValidSocialToken(credential)) {
            return AuthResult.failure("Invalid social token");
        }

        Set<CredentialType> completed = new HashSet<>();
        completed.add(CredentialType.SOCIAL);

        // OAuth는 충분히 안전한 채널 → LOW 수준
        return AuthResult.success(AuthLevel.LOW, completed);
    }

    /**
     * 소셜 토큰 검증 (실제 구현 필요)
     */
    private boolean isValidSocialToken(SocialCredential credential) {
        // TODO: 실제 OAuth 공급자 검증 로직
        return credential.getSocialUserId() != null && !credential.getSocialUserId().isEmpty();
    }
}
