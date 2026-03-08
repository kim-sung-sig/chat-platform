package com.example.chat.auth.server.api.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * TOTP 검증 요청
 */
public record VerifyTotpRequest(
        @NotBlank(message = "TOTP 코드를 입력해주세요")
        @Pattern(regexp = "^\\d{6}$", message = "TOTP 코드는 6자리 숫자여야 합니다")
        String code
) {
}
