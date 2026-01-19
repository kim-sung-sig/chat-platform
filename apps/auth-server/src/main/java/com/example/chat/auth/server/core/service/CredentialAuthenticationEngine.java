package com.example.chat.auth.server.core.service;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;

/**
 * 자격증명 인증 엔진
 * - 자격증명 타입에 따라 적절한 서비스 호출
 * - 도메인 개념만 다루며, 기술 세부사항은 서비스에 위임
 */
@Service
public class CredentialAuthenticationEngine {

    private final PasswordAuthService passwordAuthService;
    private final SocialAuthService socialAuthService;
    private final WebAuthnService webAuthnService;
    private final OtpService otpService;

    public CredentialAuthenticationEngine(PasswordAuthService passwordAuthService,
                                         SocialAuthService socialAuthService,
                                         WebAuthnService webAuthnService,
                                         OtpService otpService) {
        this.passwordAuthService = passwordAuthService;
        this.socialAuthService = socialAuthService;
        this.webAuthnService = webAuthnService;
        this.otpService = otpService;
    }

    /**
     * 자격증명 인증
     * - 자격증명 타입에 따라 적절한 서비스 선택
     * - 서비스는 "어떻게"를 담당, 엔진은 "뭘 선택할지"만 결정
     */
    public AuthResult authenticate(Credential storedCredential,
                                   Credential providedCredential,
                                   AuthenticationContext context) {

        if (providedCredential instanceof PasswordCredential pc) {
            PasswordCredential stored = (PasswordCredential) storedCredential;
            return passwordAuthService.authenticate(stored, pc, context);
        }

        if (providedCredential instanceof SocialCredential sc) {
            return socialAuthService.authenticate((SocialCredential) providedCredential, context);
        }

        if (providedCredential instanceof PasskeyCredential pkc) {
            // TODO: WebAuthn challenge와 attestation 객체 전달 필요
            return webAuthnService.authenticate(
                    (PasskeyCredential) storedCredential,
                    context,
                    "", "", ""
            );
        }

        if (providedCredential instanceof OtpCredential otpC) {
            OtpCredential stored = (OtpCredential) storedCredential;
            return otpService.verifyOtp(otpC, stored, context);
        }

        return AuthResult.failure("Unsupported credential type");
    }
}
