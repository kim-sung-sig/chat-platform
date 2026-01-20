package com.example.chat.auth.server.api;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.auth.server.api.dto.factory.CredentialFactory;
import com.example.chat.auth.server.api.dto.request.AuthenticateRequest;
import com.example.chat.auth.server.api.dto.request.CompleteMfaRequest;
import com.example.chat.auth.server.api.dto.response.AuthResponse;
import com.example.chat.auth.server.application.service.AuthenticationApplicationService;
import com.example.chat.auth.server.application.service.MfaApplicationService;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.Credential;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication Entry Point
 * - Application Service를 호출하는 인터페이스
 * - HTTP를 도메인 개념으로 변환
 * - JWT 토큰 응답
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApi {

	private final AuthenticationApplicationService authenticationService;
	private final MfaApplicationService mfaService;
	private final CredentialFactory credentialFactory;

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	/**
	 * 인증 실행 (Login)
	 */
	@PostMapping("/authenticate")
	public ResponseEntity<AuthResponse> authenticate(
			@Valid @RequestBody AuthenticateRequest requestDto,
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("Authentication request for identifier: {}, type: {}",
				requestDto.getIdentifier(), requestDto.getCredentialType());

		// 1. 도메인 개념으로 변환
		Credential providedCredential = credentialFactory.createFromRequest(requestDto);
		AuthenticationContext context = createAuthContext(request);

		// 2. 서비스 호출
		AuthenticationApplicationService.AuthenticationResult result = authenticationService.authenticate(
				requestDto.getIdentifier(),
				requestDto.getCredentialType(),
				providedCredential,
				context);

		// 3. 결과 처리
		if (result.getAuthResult().isAuthenticated()) {
			if (result.getToken() != null && result.getToken().getRefreshToken() != null) {
				setRefreshTokenCookie(response, result.getToken().getRefreshToken());
			}
		}

		return ResponseEntity.ok(AuthResponse.from(result.getAuthResult(), result.getToken()));
	}

	/**
	 * MFA 완료
	 */
	@PostMapping("/mfa/complete")
	public ResponseEntity<AuthResponse> completeMfa(
			@Valid @RequestBody CompleteMfaRequest requestDto,
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("MFA completion request for session: {}", requestDto.getMfaSessionId());

		// 1. 서비스 호출
		MfaApplicationService.MfaCompletionResult result = mfaService.completeMfa(
				requestDto.getMfaToken(),
				requestDto.getMfaSessionId(),
				requestDto.getMfaMethod(),
				requestDto.getOtpCode(),
				createAuthContext(request).getDevice());

		// 2. 결과 처리
		if (result.getAuthResult().isAuthenticated()) {
			if (result.getToken() != null && result.getToken().getRefreshToken() != null) {
				setRefreshTokenCookie(response, result.getToken().getRefreshToken());
			}
		}

		return ResponseEntity.ok(AuthResponse.from(result.getAuthResult(), result.getToken()));
	}

	/**
	 * 토큰 갱신 (Refresh Token Rotation)
	 */
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(
			@CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
			HttpServletRequest request,
			HttpServletResponse response) {

		log.info("Token refresh request");

		if (refreshToken == null || refreshToken.isEmpty()) {
			return ResponseEntity.status(401).build();
		}

		// 1. 서비스 호출
		AuthenticationApplicationService.AuthenticationResult result = authenticationService.refreshToken(
				refreshToken,
				createAuthContext(request).getDevice());

		// 2. 결과 처리
		if (result.getAuthResult().isAuthenticated()) {
			if (result.getToken() != null && result.getToken().getRefreshToken() != null) {
				setRefreshTokenCookie(response, result.getToken().getRefreshToken());
			}
		} else {
			return ResponseEntity.status(401).body(AuthResponse.from(result.getAuthResult(), null));
		}

		return ResponseEntity.ok(AuthResponse.from(result.getAuthResult(), result.getToken()));
	}

	/**
	 * 로그아웃
	 */
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
		if (channel == null)
			channel = "WEB";

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
