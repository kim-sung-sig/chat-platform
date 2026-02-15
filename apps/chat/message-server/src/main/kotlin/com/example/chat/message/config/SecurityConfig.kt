package com.example.chat.message.config

import com.example.chat.auth.jwt.annotation.EnableJwtSecurity
import com.example.chat.auth.jwt.config.SecurityRequestCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring Security 설정
 *
 * 메시지 서버 보안 정책:
 * - JWT 인증을 사용하여 모든 API 보호
 * - Health Check, Swagger UI는 인증 불필요
 * - common/security 모듈의 JWT 보안 설정 자동 적용
 */
@Configuration
@EnableJwtSecurity
class SecurityConfig {

	private val swaggerPaths = arrayOf(
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html"
	)

	private val healthCheckPaths = arrayOf(
		"/actuator/health",
		"/actuator/info"
	)

	@Bean
	fun securityRequestCustomizer(): SecurityRequestCustomizer {
		return SecurityRequestCustomizer { auth ->
			auth.requestMatchers(*swaggerPaths).permitAll()
			auth.requestMatchers(*healthCheckPaths).permitAll()
			auth.requestMatchers("/api/messages/health").permitAll()
		}
	}
}
