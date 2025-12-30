package com.example.chat.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * <p>
 * 메시지 서버 보안 정책:
 * - Health Check: 인증 불필요
 * - Swagger UI: 개발 환경에서 허용
 * - API 엔드포인트: 임시로 모든 요청 허용 (추후 JWT 인증 추가 예정)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						// Health Check - 인증 불필요
						.requestMatchers("/api/messages/health", "/health").permitAll()
						// Swagger UI - 개발 환경에서 허용
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						// 임시로 모든 요청 허용 (추후 JWT 인증 추가)
						.anyRequest().permitAll()
				);

		return http.build();
	}
}
