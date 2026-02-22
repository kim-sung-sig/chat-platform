package com.example.chat.auth.server.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * MFA 완료 요청 DTO
 */
public record CompleteMfaRequest(
        @NotBlank(message = "mfaToken cannot be blank") String mfaToken,
        @NotBlank(message = "mfaSessionId cannot be blank") String mfaSessionId,
        @NotBlank(message = "mfaMethod cannot be blank") String mfaMethod,
        @NotBlank(message = "otpCode cannot be blank") String otpCode) {
}
