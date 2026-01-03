package com.example.chat.common.security.jwt.error;

import com.example.chat.common.core.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * JWT Í¥Ä???àÏô∏Î•?Ï≤òÎ¶¨?òÎäî Í∏ÄÎ°úÎ≤å ?àÏô∏ ?∏Îì§??
 * ???¥Îûò?§Îäî Ïª®Ìä∏Î°§Îü¨ ?àÎ≤®?êÏÑú Î∞úÏÉù???∏Ï¶ù/?∏Í? ?àÏô∏Î•??°ÏïÑ??JSON ?ëÎãµ?ºÎ°ú Î≥Ä?òÌï©?àÎã§.
 */
@Slf4j
@RestControllerAdvice
public class JwtExceptionHandler {

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, HttpServletRequest request) {
		log.error("Auth exception occurred: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.of(ex, request.getRequestURI());
		return errorResponse.toResponseEntity();
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
		log.error("Authentication exception occurred: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.INVALID_TOKEN, request.getRequestURI());
		return errorResponse.toResponseEntity();
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
		log.error("Access denied exception occurred: {}", ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.INSUFFICIENT_SCOPE, request.getRequestURI());
		return errorResponse.toResponseEntity();
	}
}
