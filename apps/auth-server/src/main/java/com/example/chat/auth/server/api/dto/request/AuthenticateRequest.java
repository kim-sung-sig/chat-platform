package com.example.chat.auth.server.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인증 요청 DTO
 * - 모든 로그인 방식이 이 요청으로 통합
 * - credentialType과 credentialData의 조합으로 로그인 방식 결정
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateRequest {

    @NotBlank(message = "identifier cannot be blank")
    private String identifier;  // email, username 등

    @NotBlank(message = "credentialType cannot be blank")
    private String credentialType;  // PASSWORD, SOCIAL, PASSKEY, OTP

    @NotBlank(message = "credentialData cannot be blank")
    private String credentialData;  // 방식별 자격증명 데이터
                                     // PASSWORD: 평문 비밀번호
                                     // SOCIAL: OAuth 토큰
                                     // PASSKEY: WebAuthn 응답
                                     // OTP: OTP 코드
}
