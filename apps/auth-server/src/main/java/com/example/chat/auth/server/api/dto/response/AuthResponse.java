package com.example.chat.auth.server.api.dto.response;

import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.MfaType;
import com.example.chat.auth.server.core.domain.Token;
import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record AuthResponse(
        boolean authenticated,
        String authLevel,
        Set<String> completedCredentials,
        boolean requiresMfa,
        String mfaSessionId,
        List<String> remainingMfaMethods,
        String failureReason,
        TokenResponse token) {
    public static AuthResponse from(AuthResult result, Token token) {
        long expiresIn = token != null
                ? Duration.between(Instant.now(), token.expiresAt()).toSeconds()
                : 0L;

        return AuthResponse.builder()
                .authenticated(result.authenticated())
                .authLevel(result.authLevel() != null ? result.authLevel().name() : null)
                .completedCredentials(result.completedCredentials().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .requiresMfa(result.requiresMfa())
                .mfaSessionId(result.mfaRequirement() != null ? result.mfaRequirement().sessionId() : null)
                .remainingMfaMethods(result.mfaRequirement() != null
                        ? result.mfaRequirement().getRemainingMethods().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList())
                        : null)
                .failureReason(result.failureReason())
                .token(token != null ? TokenResponse.builder()
                        .accessToken(token.accessToken())
                        .refreshToken(token.refreshToken())
                        .tokenType(token.tokenType().name())
                        .expiresIn(expiresIn)
                        .build() : null)
                .build();
    }
}
