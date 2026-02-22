package com.example.chat.auth.server.api.dto.response;

import java.util.UUID;

public record SignupResponse(
        UUID principalId,
        String identifier,
        String message) {
    public SignupResponse(UUID principalId, String identifier) {
        this(principalId, identifier, "회원가입이 완료되었습니다");
    }
}
