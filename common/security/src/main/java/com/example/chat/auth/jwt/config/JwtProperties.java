package com.example.chat.auth.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 보안 설정 프로퍼티
 *
 * - secretKey 가 있으면 HS256 대칭키 방식으로 검증
 * - jwkSetUri 가 있으면 JWK Set URI 방식 (RS256/ES256 비대칭키) 으로 검증
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String issuerUri, String jwkSetUri, String secretKey) {

    /** HS256 대칭키 방식 여부 */
    public boolean isHmacMode() {
        return secretKey != null && !secretKey.isBlank();
    }
}
