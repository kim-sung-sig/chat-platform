package com.example.chat.auth.server.mfa.domain.service;

import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthResult;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.auth.domain.CredentialType;
import com.example.chat.auth.server.auth.domain.credential.PasskeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * WebAuthn / Passkey authentication service
 */
@Service
public class WebAuthnService {

    private final boolean webAuthnEnabled;

    public WebAuthnService(@Value("${auth.webauthn.enabled:false}") boolean webAuthnEnabled) {
        this.webAuthnEnabled = webAuthnEnabled;
    }

    /** Passkey verify */
    public AuthResult authenticate(
            PasskeyCredential credential,
            AuthenticationContext context,
            String challenge,
            String clientData,
            String attestationObject) {
        if (!webAuthnEnabled) {
            throw new AuthException(AuthServerErrorCode.FEATURE_DISABLED);
        }
        if (isBlank(challenge) || isBlank(clientData) || isBlank(attestationObject)) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }
        if (!verifySignature(credential, challenge, clientData, attestationObject)) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }

        return AuthResult.success(
                AuthLevel.HIGH,
                Collections.singleton(CredentialType.PASSKEY));
    }

    /** Challenge generate */
    public String generateChallenge() {
        return UUID.randomUUID().toString();
    }

    /** Signature verification */
    private boolean verifySignature(
            PasskeyCredential credential,
            String challenge,
            String clientData,
            String attestationObject) {
        // TODO: Implement real WebAuthn verification. Fail closed until implemented.
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
