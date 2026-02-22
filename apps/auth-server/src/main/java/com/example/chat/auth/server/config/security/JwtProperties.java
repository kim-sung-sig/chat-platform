package com.example.chat.auth.server.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 Properties
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String issuer = "http://localhost:18080";
    private long accessTokenExpiration = 3600L;
    private long refreshTokenExpiration = 86400L;
}
