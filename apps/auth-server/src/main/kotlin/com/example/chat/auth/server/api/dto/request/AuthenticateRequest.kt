package com.example.chat.auth.server.api.dto.request

import jakarta.validation.constraints.NotBlank

/** 인증 요청 DTO */
data class AuthenticateRequest(
        @field:NotBlank(message = "identifier cannot be blank") val identifier: String? = null,
        @field:NotBlank(message = "credentialType cannot be blank")
        val credentialType: String? = null,
        @field:NotBlank(message = "credentialData cannot be blank")
        val credentialData: String? = null
)
