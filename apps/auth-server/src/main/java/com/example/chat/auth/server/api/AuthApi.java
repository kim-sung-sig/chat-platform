package com.example.chat.auth.server.api;

import com.example.chat.auth.server.api.dto.factory.CredentialFactory;
import com.example.chat.auth.server.api.dto.request.AuthenticateRequest;
import com.example.chat.auth.server.api.dto.request.CompleteMfaRequest;
import com.example.chat.auth.server.api.dto.request.SignupRequest;
import com.example.chat.auth.server.api.dto.response.AuthResponse;
import com.example.chat.auth.server.api.dto.response.SignupResponse;
import com.example.chat.auth.server.application.service.AuthenticationApplicationService;
import com.example.chat.auth.server.application.service.MfaApplicationService;
import com.example.chat.auth.server.application.service.SignupApplicationService;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthenticationApplicationService authenticationService;
    private final MfaApplicationService mfaService;
    private final CredentialFactory credentialFactory;
    private final SignupApplicationService signupService;

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request for: {}", request.email());
        SignupResponse response = signupService.signup(request);
        return ResponseEntity.status(201).body(response);
    }

    /** 인증 실행 (Login) */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthenticateRequest requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("Authentication request for identifier: {}, type: {}", requestDto.identifier(),
                requestDto.credentialType());

        Credential providedCredential = credentialFactory.createFromRequest(requestDto);
        AuthenticationContext context = createAuthContext(request);

        AuthenticationApplicationService.AuthenticationResult result = authenticationService.authenticate(
                requestDto.identifier(),
                CredentialType.valueOf(requestDto.credentialType().toUpperCase()),
                providedCredential,
                context);

        if (result.authResult().authenticated()) {
            if (result.token() != null && result.token().refreshToken() != null) {
                setRefreshTokenCookie(response, result.token().refreshToken());
            }
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult(), result.token()));
    }

    /** MFA 완료 */
    @PostMapping("/mfa/complete")
    public ResponseEntity<AuthResponse> completeMfa(
            @Valid @RequestBody CompleteMfaRequest requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("MFA completion request for session: {}", requestDto.mfaSessionId());

        MfaApplicationService.MfaCompletionResult result = mfaService.completeMfa(
                requestDto.mfaToken(),
                requestDto.mfaSessionId(),
                requestDto.mfaMethod(),
                requestDto.otpCode(),
                createAuthContext(request).getDevice());

        if (result.authResult().authenticated()) {
            if (result.token() != null && result.token().refreshToken() != null) {
                setRefreshTokenCookie(response, result.token().refreshToken());
            }
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult(), result.token()));
    }

    /** 토큰 갱신 (Refresh Token Rotation) */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("Token refresh request");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        AuthenticationApplicationService.AuthenticationResult result =
                authenticationService.refreshToken(refreshToken, createAuthContext(request).getDevice());

        if (result.authResult().authenticated()) {
            if (result.token() != null && result.token().refreshToken() != null) {
                setRefreshTokenCookie(response, result.token().refreshToken());
            }
        } else {
            return ResponseEntity.status(401).body(AuthResponse.from(result.authResult(), null));
        }

        return ResponseEntity.ok(AuthResponse.from(result.authResult(), result.token()));
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("Logout request");

        authenticationService.logout(refreshToken);
        deleteRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }

    private AuthenticationContext createAuthContext(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String channel = request.getHeader("X-Channel");
        if (channel == null) channel = "WEB";

        return new AuthenticationContext(
                ipAddress,
                userAgent != null ? userAgent : "Unknown",
                channel,
                Instant.now(),
                false);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true) // Production에서는 true
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
