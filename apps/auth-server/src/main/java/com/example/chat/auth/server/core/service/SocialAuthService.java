package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import com.example.chat.auth.server.core.service.oauth.SocialOAuth2Service;
import com.example.chat.auth.server.core.service.oauth.SocialType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 소셜(OAuth) 인증 서비스
 */
@Service
public class SocialAuthService {
    private final Map<SocialType, SocialOAuth2Service> socialServices;

    public SocialAuthService(List<SocialOAuth2Service> socialServicesList) {
        this.socialServices = socialServicesList.stream()
                .collect(Collectors.toMap(SocialOAuth2Service::getSocialType, service -> service));
    }

    /** 소셜 인증 처리 */
    public AuthResult authenticate(SocialCredential credential, AuthenticationContext context) {
        SocialType socialType;
        try {
            socialType = SocialType.valueOf(credential.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException(
                    AuthServerErrorCode.SOCIAL_AUTH_FAILED,
                    new Object[] { "Unsupported social provider: " + credential.getProvider() });
        }

        SocialOAuth2Service socialOAuth2Service = socialServices.get(socialType);
        if (socialOAuth2Service == null) {
            throw new AuthException(
                    AuthServerErrorCode.SOCIAL_AUTH_FAILED,
                    new Object[] { "Social service not configured for: " + socialType });
        }

        try {
            socialOAuth2Service.getUserInfo(credential.getSocialUserId());
            return AuthResult.success(
                    AuthLevel.LOW,
                    Collections.singleton(CredentialType.SOCIAL));
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.SOCIAL_AUTH_FAILED, null, e);
        }
    }
}
