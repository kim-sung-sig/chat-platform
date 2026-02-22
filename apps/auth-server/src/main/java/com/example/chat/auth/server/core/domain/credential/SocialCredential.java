package com.example.chat.auth.server.core.domain.credential;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;
import lombok.Getter;

/**
 * 소셜 계정 자격증명
 */
@Getter
public final class SocialCredential extends Credential {
    private final String provider;
    private final String socialUserId;
    private final String email;

    public SocialCredential(String provider, String socialUserId, String email, boolean verified) {
        super(CredentialType.SOCIAL, verified);
        this.provider = provider;
        this.socialUserId = socialUserId;
        this.email = email;
    }

    @Override
    public AuthLevel minAuthLevel() {
        return AuthLevel.LOW;
    }
}
