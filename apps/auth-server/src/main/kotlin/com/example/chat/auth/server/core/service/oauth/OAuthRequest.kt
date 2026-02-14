package com.example.chat.auth.server.core.service.oauth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true)
data class OAuthRequest(
        @field:NotBlank val provider: SocialType,
        @field:NotBlank val code: String,
        val state: String? = null,
        val redirectUri: String? = null
)
