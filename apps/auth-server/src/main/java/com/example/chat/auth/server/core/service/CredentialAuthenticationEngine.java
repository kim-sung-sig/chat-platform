package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 자격증명 인증 엔진
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialAuthenticationEngine {

    private final PasswordAuthService passwordAuthService;
    private final SocialAuthService socialAuthService;
    private final WebAuthnService webAuthnService;
    private final OtpService otpService;

    /** 자격증명 인증 */
    public AuthResult authenticate(
            Credential storedCredential,
            Credential providedCredential,
            AuthenticationContext context) {
        if (providedCredential instanceof PasswordCredential p) {
            return passwordAuthService.authenticate((PasswordCredential) storedCredential, p, context);
        } else if (providedCredential instanceof SocialCredential s) {
            return socialAuthService.authenticate(s, context);
        } else if (providedCredential instanceof PasskeyCredential p) {
            return webAuthnService.authenticate(p, context, "", "", "");
        } else if (providedCredential instanceof OtpCredential o) {
            return otpService.verifyOtp(o, (OtpCredential) storedCredential, context);
        } else {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }
    }
}
