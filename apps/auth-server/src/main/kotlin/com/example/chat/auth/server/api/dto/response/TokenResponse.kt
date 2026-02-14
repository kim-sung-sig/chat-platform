package com.example.chat.auth.server.api.dto.response

data class TokenResponse(
        val accessToken: String,
        val refreshToken: String?,
        val tokenType: String,
        val expiresIn: Long
)
