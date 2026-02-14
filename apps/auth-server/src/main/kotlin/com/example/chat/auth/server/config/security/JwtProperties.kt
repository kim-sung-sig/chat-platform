
package com.example.chat.auth.server.config.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 설정 Properties
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var issuer: String = "http://localhost:18080",
    var accessTokenExpiration: Long = 3600L,
    var refreshTokenExpiration: Long = 86400L
)
