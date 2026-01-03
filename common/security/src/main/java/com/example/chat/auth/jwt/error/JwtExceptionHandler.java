package com.example.chat.auth.jwt.error;

import com.example.chat.common.core.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * JWT 관련 예외를 처리하는 글로벌 예외 핸들러
 * 이 클래스는 컨트롤러 레벨에서 발생한 인증/인가 예외를 잡아서 JSON 응답으로 변환합니다.
 */
@Slf4j
@RestControllerAdvice
public class JwtExceptionHandler {

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, HttpServletRequest request) {
		log.error("Auth exception occurred: {}", ex.getMessage());

		var errorResponse = ErrorResponse.of(ex, request.getRequestURI());
		return toResponseEntity(errorResponse);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
		log.error("Authentication exception occurred: {}", ex.getMessage());

		var errorResponse = ErrorResponse.of(AuthErrorCode.INVALID_TOKEN, request.getRequestURI());
		return toResponseEntity(errorResponse);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
		log.error("Access denied exception occurred: {}", ex.getMessage());

		var errorResponse = ErrorResponse.of(AuthErrorCode.INSUFFICIENT_SCOPE, request.getRequestURI());
		return toResponseEntity(errorResponse);
	}

	private ResponseEntity<ErrorResponse> toResponseEntity(ErrorResponse errorResponse) {
		return ResponseEntity
				.status(errorResponse.getStatus())
				.body(errorResponse);
	}
}
