package com.example.chat.auth.server.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
