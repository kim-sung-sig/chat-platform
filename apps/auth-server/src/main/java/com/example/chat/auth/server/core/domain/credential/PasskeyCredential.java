package com.example.chat.auth.server.core.domain.credential;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;
import lombok.Getter;

/**
 * Passkey / WebAuthn 자격증명
 */
@Getter
public final class PasskeyCredential extends Credential {
    private final String credentialId;
    private final String publicKey;
    private final String authenticatorName;

    public PasskeyCredential(String credentialId, String publicKey, String authenticatorName, boolean verified) {
        super(CredentialType.PASSKEY, verified);
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.authenticatorName = authenticatorName;
    }

    @Override
    public AuthLevel minAuthLevel() {
        return AuthLevel.HIGH;
    }
}
