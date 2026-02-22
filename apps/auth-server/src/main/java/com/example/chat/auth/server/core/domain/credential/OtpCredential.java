package com.example.chat.auth.server.core.domain.credential;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;
import lombok.Getter;

/**
 * OTP 자격증명
 */
@Getter
public final class OtpCredential extends Credential {
    private final String code;
    private final String deliveryMethod;

    public OtpCredential(String code, String deliveryMethod) {
        super(CredentialType.OTP, false);
        this.code = code;
        this.deliveryMethod = deliveryMethod;
    }

    @Override
    public AuthLevel minAuthLevel() {
        return AuthLevel.LOW;
    }
}
