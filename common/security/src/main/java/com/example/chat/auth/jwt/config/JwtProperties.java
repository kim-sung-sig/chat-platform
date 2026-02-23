package com.example.chat.auth.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 보안 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String issuerUri, String jwkSetUri) {
}
