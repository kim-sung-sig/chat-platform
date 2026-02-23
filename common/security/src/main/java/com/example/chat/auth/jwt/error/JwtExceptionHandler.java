package com.example.chat.auth.jwt.error;

import com.example.chat.common.web.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * JWT 관련 예외를 처리하는 전역 예외 핸들러
 *
 * 발생한 인증 예외를 JSON 응답으로 변환합니다.
 */
@RestControllerAdvice
public class JwtExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(JwtExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        log.error("Auth exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex);
        return errorResponse.toResponseEntity();
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.INVALID_TOKEN);
        return errorResponse.toResponseEntity();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied exception occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.INSUFFICIENT_SCOPE);
        return errorResponse.toResponseEntity();
    }
}
