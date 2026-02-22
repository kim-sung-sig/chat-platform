package com.example.chat.auth.server.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증의 신뢰 수준
 */
@Getter
@RequiredArgsConstructor
public enum AuthLevel {
    LOW(1, "Password only"),
    MEDIUM(2, "Password + OTP"),
    HIGH(3, "Passkey / WebAuthn");

    private final int level;
    private final String description;

    /**
     * 이 수준이 다른 수준보다 높은가?
     */
    public boolean isHigherOrEqual(AuthLevel other) {
        return this.level >= other.level;
    }

    /**
     * 이 수준이 다른 수준보다 낮은가?
     */
    public boolean isLowerOrEqual(AuthLevel other) {
        return this.level <= other.level;
    }
}
