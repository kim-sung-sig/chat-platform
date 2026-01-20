package com.example.chat.auth.server.core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import com.example.chat.auth.server.core.service.oauth.SocialOAuth2Service;
import com.example.chat.auth.server.core.service.oauth.SocialType;

/**
 * 소셜(OAuth) 인증 서비스
 * - OAuth 공급자(Google, Kakao, Apple 등)와 통신
 * - 토큰 검증
 * - 서비스가 "어떤 OAuth 공급자와 통신할 것인가" 결정
 */
@Service
public class SocialAuthService {

    private final Map<SocialType, SocialOAuth2Service> socialServices;

    public SocialAuthService(List<SocialOAuth2Service> socialServices) {
        this.socialServices = socialServices.stream()
                .collect(Collectors.toMap(SocialOAuth2Service::getSocialType, Function.identity()));
    }

    /**
     * 소셜 인증 처리
     * 실제로는 OAuth 클라이언트 라이브러리를 사용하여 토큰 검증
     */
    public AuthResult authenticate(SocialCredential credential,
            AuthenticationContext context) {

        SocialType socialType;
        try {
            socialType = SocialType.valueOf(credential.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AuthResult.failure("Unsupported social provider: " + credential.getProvider());
        }

        SocialOAuth2Service socialOAuth2Service = socialServices.get(socialType);
        if (socialOAuth2Service == null) {
            return AuthResult.failure("Social service not configured for: " + socialType);
        }

        // 실제 구현에서는 credential.getSocialUserId()가 accessToken 역할을 하거나
        // 별도의 OAuthRequest를 통해 처리해야 함.
        // 여기서는 credential.getSocialUserId()를 accessToken으로 가정하고 검증 시도 (예시)
        try {
            socialOAuth2Service.getUserInfo(credential.getSocialUserId());
        } catch (Exception e) {
            return AuthResult.failure("Invalid social token: " + e.getMessage());
        }

        Set<CredentialType> completed = new HashSet<>();
        completed.add(CredentialType.SOCIAL);

        // OAuth는 충분히 안전한 채널 → LOW 수준
        return AuthResult.success(AuthLevel.LOW, completed);
    }
}
