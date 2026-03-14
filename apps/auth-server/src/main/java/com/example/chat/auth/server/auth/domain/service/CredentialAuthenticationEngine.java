package com.example.chat.auth.server.auth.domain.service;

import com.example.chat.auth.server.mfa.domain.service.OtpService;
import com.example.chat.auth.server.mfa.domain.service.WebAuthnService;
import com.example.chat.auth.server.oauth.domain.service.SocialAuthService;
import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.AuthResult;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.auth.domain.Credential;
import com.example.chat.auth.server.auth.domain.credential.OtpCredential;
import com.example.chat.auth.server.auth.domain.credential.PasskeyCredential;
import com.example.chat.auth.server.auth.domain.credential.PasswordCredential;
import com.example.chat.auth.server.auth.domain.credential.SocialCredential;
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
	    return switch (providedCredential) {
		    case PasswordCredential p -> passwordAuthService.authenticate((PasswordCredential) storedCredential, p, context);
		    case SocialCredential s -> socialAuthService.authenticate(s, context);
		    case PasskeyCredential p -> webAuthnService.authenticate(p, context, "", "", "");
		    case OtpCredential o -> otpService.verifyOtp(o, (OtpCredential) storedCredential, context);
		    case null, default -> throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
	    };
    }
}
