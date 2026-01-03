package com.example.chat.auth.server.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 Properties
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
		String issuer,
		Long accessTokenExpiration,
		Long refreshTokenExpiration
) {
	public JwtProperties {
		if (issuer == null || issuer.isBlank()) {
			issuer = "http://localhost:18080";
		}
		if (accessTokenExpiration == null) {
			accessTokenExpiration = 3600L; // 1 hour
		}
		if (refreshTokenExpiration == null) {
			refreshTokenExpiration = 86400L; // 24 hours
		}
	}
}

