package com.example.chat.auth.jwt.config;

import com.example.chat.auth.jwt.error.AuthErrorCode;
import com.example.chat.auth.jwt.error.AuthExceptions;
import com.example.chat.auth.jwt.error.JwtAuthenticationException;
import com.example.chat.common.core.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 실패 시 에러 응답을 처리하는 핸들러
 */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {

		log.error("JWT Authentication failed: {}", authException.getMessage());

		JwtAuthenticationException authEx;
		if (authException instanceof OAuth2AuthenticationException oauth2Exception) {
			authEx = AuthExceptions.fromOAuth2Exception(oauth2Exception);
		} else {
			authEx = AuthExceptions.of(AuthErrorCode.INVALID_TOKEN);
		}

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ErrorResponse errorResponse = ErrorResponse.of(authEx, request.getRequestURI());

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
