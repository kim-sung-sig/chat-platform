package com.example.chat.auth.server.rest.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/**
 * 비밀번호 변경 요청
 */
public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword,
        @NotBlank @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다")
        String newPassword
) {
}
