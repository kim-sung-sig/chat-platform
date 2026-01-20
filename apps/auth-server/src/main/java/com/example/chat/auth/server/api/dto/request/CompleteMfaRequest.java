package com.example.chat.auth.server.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MFA 완료 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMfaRequest {

    @NotBlank(message = "mfaToken cannot be blank")
    private String mfaToken; // MFA_PENDING JWT 토큰

    @NotBlank(message = "mfaSessionId cannot be blank")
    private String mfaSessionId; // 1차 인증에서 받은 MFA 세션 ID

    @NotBlank(message = "mfaMethod cannot be blank")
    private String mfaMethod; // OTP, BACKUP_CODE

    @NotBlank(message = "otpCode cannot be blank")
    private String otpCode; // MFA 코드
}
