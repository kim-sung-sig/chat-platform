package com.example.chat.auth.jwt.config;

import com.example.chat.auth.jwt.error.AuthErrorCode;
import com.example.chat.auth.jwt.error.AuthExceptions;
import com.example.chat.common.web.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 실패 시 에러 응답을 처리하는 EntryPoint
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.error("JWT Authentication failed: {}", authException.getMessage());

        var authEx = (authException instanceof OAuth2AuthenticationException oAuth2Ex)
                ? AuthExceptions.fromOAuth2Exception(oAuth2Ex)
                : AuthExceptions.of(AuthErrorCode.INVALID_TOKEN);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(authEx);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
