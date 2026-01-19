package com.example.chat.auth.server.core.domain.credential;

import java.util.Objects;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;

/**
 * 소셜 계정 자격증명
 * - 연결된 소셜 제공자 정보만 도메인에서 관심
 * - OAuth 토큰 검증은 서비스 계층의 책임
 */
public class SocialCredential extends Credential {

    private final String provider;           // Google, Kakao, Apple
    private final String socialUserId;       // 제공자의 사용자 ID
    private final String email;

    public SocialCredential(String provider, String socialUserId, String email, boolean verified) {
        super(CredentialType.SOCIAL, verified);
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.socialUserId = Objects.requireNonNull(socialUserId, "socialUserId cannot be null");
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public String getSocialUserId() {
        return socialUserId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public AuthLevel minAuthLevel() {
        // OAuth는 일반적으로 안전한 통신을 통해 검증됨
        return AuthLevel.LOW;
    }
}
