package com.example.chat.auth.server.core.domain.credential;

import java.util.Objects;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;

/**
 * Passkey / WebAuthn 자격증명
 * - 실제 검증은 WebAuthn 프로토콜로 수행 (서비스 계층)
 * - 도메인은 "이런 자격증명이 등록되어 있다"만 표현
 */
public class PasskeyCredential extends Credential {

    private final String credentialId;
    private final String publicKey;
    private final String authenticatorName;

    public PasskeyCredential(String credentialId, String publicKey,
                           String authenticatorName, boolean verified) {
        super(CredentialType.PASSKEY, verified);
        this.credentialId = Objects.requireNonNull(credentialId, "credentialId cannot be null");
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey cannot be null");
        this.authenticatorName = authenticatorName;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    @Override
    public AuthLevel minAuthLevel() {
        // Passkey는 가장 강력한 인증 방식
        return AuthLevel.HIGH;
    }
}
