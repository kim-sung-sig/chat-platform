package com.example.chat.auth.server.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String nickname
) {
    public SignupRequest {
        
    }
}
