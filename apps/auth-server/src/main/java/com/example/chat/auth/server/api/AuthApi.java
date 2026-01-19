package com.example.chat.auth.server.api;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

	/**
	 * 로그인
	 * - 비밀번호, OAuth, Passkey, SSO 모두 이 엔드포인트로 처리
	 * - JWT 토큰 응답 (FULL_ACCESS 또는 MFA_PENDING)
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody AuthenticateRequest request,
			HttpServletRequest httpRequest) {
		
		log.info("Login attempt for identifier: {}, type: {}", 
				request.getIdentifier(), request.getCredentialType());

		// 1. HTTP → 도메인 개념 변환
		AuthenticationContext context = buildAuthenticationContext(httpRequest);

		// 2. Credential 구성 (Factory 패턴)
		Credential credential = credentialFactory.createFromRequest(request);
		
		// 3. Application Service 실행
		AuthenticationApplicationService.AuthenticationResult result = 
				authenticationService.authenticate(
						request.getIdentifier(),
						request.getCredentialType(),
						credential,
						context
				);

		// 4. 도메인 → HTTP 응답 변환 (JWT 포함)
		return ResponseEntity.ok(AuthResponse.from(result.getAuthResult(), result.getToken()));
	}

	/**
	 * MFA 검증
	 * - 1차 인증 후 MFA가 필요한 경우 호출
	 * - MFA_PENDING 토큰을 헤더로 받음
	 * - 성공 시 FULL_ACCESS 토큰 반환
	 */
	@PostMapping("/mfa/complete")
	public ResponseEntity<AuthResponse> completeMfa(
			@RequestHeader("Authorization") String authorization,
			@Valid @RequestBody CompleteMfaRequest request) {
		
		log.info("MFA completion for session: {}", request.getSessionId());

		// Bearer 토큰 추출
		String mfaToken = authorization.replace("Bearer ", "");

		// MFA 완료 처리
		MfaApplicationService.MfaCompletionResult result = mfaService.completeMfa(
				mfaToken,
				request.getSessionId(),
				request.getMfaMethod(),
				request.getCode()
		);

		return ResponseEntity.ok(AuthResponse.from(result.getAuthResult(), result.getToken()));
	}

	/**
	 * HTTP 요청을 AuthenticationContext로 변환
	 */
	private AuthenticationContext buildAuthenticationContext(HttpServletRequest request) {
		String ipAddress = extractClientIp(request);
		String userAgent = request.getHeader("User-Agent") != null 
				? request.getHeader("User-Agent") 
				: "Unknown";
		String channel = determineChannel(request);

		return new AuthenticationContext(
				ipAddress,
				userAgent,
				channel,
				Instant.now(),
				false  // TODO: 실제 위험 감지 로직
		);
	}

	private String extractClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0];
		}
		return request.getRemoteAddr();
	}

	private String determineChannel(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent") != null 
				? request.getHeader("User-Agent").toLowerCase() 
				: "";
		
		if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
			return "MOBILE_APP";
		}
		return "WEB";
	}
}
