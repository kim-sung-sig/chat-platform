package com.example.chat.auth.server.api.dto.response;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.Token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인증 응답 DTO
 * - 도메인의 AuthResult를 HTTP 응답으로 변환
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private boolean authenticated;
    private String authLevel; // LOW, MEDIUM, HIGH
    private Set<String> completedCredentials;
    private boolean requiresMfa;
    private String mfaSessionId; // MFA 필요시 세션 ID
    private String[] remainingMfaMethods; // MFA 필요시 남은 방식
    private String failureReason;
    private TokenResponse token; // 인증 성공시 토큰 정보

    /**
     * AuthResult를 AuthResponse로 변환
     */
    public static AuthResponse from(AuthResult result) {
        return from(result, null);
    }

    /**
     * AuthResult와 Token을 AuthResponse로 변환
     */
    public static AuthResponse from(AuthResult result, Token token) {
        AuthResponseBuilder builder = AuthResponse.builder()
                .authenticated(result.isAuthenticated())
                .requiresMfa(result.requiresMfa())
                .failureReason(result.getFailureReason());

        if (result.getAuthLevel() != null) {
            builder.authLevel(result.getAuthLevel().name());
        }

        if (result.getCompletedCredentials() != null) {
            builder.completedCredentials(
                    result.getCompletedCredentials().stream()
                            .map(CredentialType::name)
                            .collect(java.util.stream.Collectors.toSet()));
        }

        if (result.requiresMfa() && result.getMfaRequirement() != null) {
            builder.mfaSessionId(result.getMfaRequirement().getSessionId());
            builder.remainingMfaMethods(
                    result.getMfaRequirement().getRemainingMethods().stream()
                            .map(Enum::name)
                            .toArray(String[]::new));
        }

        if (token != null) {
            builder.token(TokenResponse.builder()
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .tokenType(token.getTokenType().name())
                    .expiresIn(Duration.between(Instant.now(), token.getExpiresAt()).toSeconds())
                    .build());
        }

        return builder.build();
    }
}
