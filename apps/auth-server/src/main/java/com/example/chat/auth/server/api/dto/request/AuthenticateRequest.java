package com.example.chat.auth.server.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 인증 요청 DTO
 */
public record AuthenticateRequest(
        @NotBlank(message = "identifier cannot be blank") String identifier,
        @NotBlank(message = "credentialType cannot be blank") String credentialType,
        @NotBlank(message = "credentialData cannot be blank") String credentialData) {
}
