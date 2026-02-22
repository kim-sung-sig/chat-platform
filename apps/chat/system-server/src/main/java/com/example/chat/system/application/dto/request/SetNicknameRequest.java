package com.example.chat.system.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 친구 별칭 설정 Request DTO
 */
public record SetNicknameRequest(
        @NotBlank(message = "Nickname is required") @Size(max = 100, message = "Nickname must be less than 100 characters") String nickname) {
}
