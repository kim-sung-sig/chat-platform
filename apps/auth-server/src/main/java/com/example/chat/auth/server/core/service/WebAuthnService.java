package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * WebAuthn / Passkey 인증 서비스
 */
@Service
public class WebAuthnService {

    /** Passkey 검증 */
    public AuthResult authenticate(
            PasskeyCredential credential,
            AuthenticationContext context,
            String challenge,
            String clientData,
            String attestationObject) {
        if (!verifySignature(credential, challenge, clientData, attestationObject)) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }

        return AuthResult.success(
                AuthLevel.HIGH,
                Collections.singleton(CredentialType.PASSKEY));
    }

    /** Challenge 생성 */
    public String generateChallenge() {
        return UUID.randomUUID().toString();
    }

    /** 서명 검증 */
    private boolean verifySignature(
            PasskeyCredential credential,
            String challenge,
            String clientData,
            String attestationObject) {
        // TODO: WebAuthn 라이브러리로 검증
        return true;
    }
}
