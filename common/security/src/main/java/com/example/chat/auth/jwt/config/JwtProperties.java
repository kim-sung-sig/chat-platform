package com.example.chat.auth.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * JWT 보안 설정 프로퍼티
 * <p>
 * Java Record를 사용하여 불변 설정 객체를 구현합니다.
 * Spring Boot 3.x에서는 Record가 @ConfigurationProperties와 완벽하게 동작합니다.
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
		String issuerUri,
		String jwkSetUri
) {
	/**
	 * Compact Constructor - 기본값 설정 및 검증
	 */
	public JwtProperties {
		Objects.requireNonNull(issuerUri);
		Objects.requireNonNull(jwkSetUri);
	}
}