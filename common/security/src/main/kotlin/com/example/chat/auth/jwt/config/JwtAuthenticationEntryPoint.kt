package com.example.chat.auth.jwt.config

import com.example.chat.auth.jwt.error.AuthErrorCode
import com.example.chat.auth.jwt.error.AuthExceptions
import com.example.chat.common.web.response.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

private val log = KotlinLogging.logger {}

/** JWT 인증 실패 시 에러 응답을 처리하는 핸들러 */
class JwtAuthenticationEntryPoint(private val objectMapper: ObjectMapper) :
        AuthenticationEntryPoint {
    override fun commence(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authException: AuthenticationException
    ) {
        log.error { "JWT Authentication failed: ${authException.message}" }

        val authEx =
                if (authException is OAuth2AuthenticationException) {
                    AuthExceptions.fromOAuth2Exception(authException)
                } else {
                    AuthExceptions.of(AuthErrorCode.INVALID_TOKEN)
                }

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val errorResponse = ErrorResponse.of(authEx)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
