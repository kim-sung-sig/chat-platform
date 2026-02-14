package com.example.chat.auth.jwt.error

import com.example.chat.common.web.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

/** JWT 관련 예외를 처리하는 글로벌 예외 핸들러 이 클래스는 컨트롤러 레벨에서 발생한 인증/인가 예외를 잡아서 JSON 응답으로 변환합니다. */
@RestControllerAdvice
class JwtExceptionHandler {
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): ResponseEntity<ErrorResponse> {
        log.error { "Auth exception occurred: ${ex.message}" }

        val errorResponse = ErrorResponse.of(ex)
        return errorResponse.toResponseEntity()
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ErrorResponse> {
        log.error { "Authentication exception occurred: ${ex.message}" }

        val errorResponse = ErrorResponse.of(AuthErrorCode.INVALID_TOKEN)
        return errorResponse.toResponseEntity()
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.error { "Access denied exception occurred: ${ex.message}" }

        val errorResponse = ErrorResponse.of(AuthErrorCode.INSUFFICIENT_SCOPE)
        return errorResponse.toResponseEntity()
    }
}
