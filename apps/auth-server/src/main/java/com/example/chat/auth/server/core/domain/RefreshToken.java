package com.example.chat.auth.server.core.domain;

import com.example.chat.auth.server.core.domain.credential.Device;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class RefreshToken {
    private final UUID id;
    private final UUID principalId;
    private final Device device;
    private String tokenValue;
    private Instant expiryAt;
    private final Instant createdAt;
    private Instant lastUsedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiryAt);
    }

    public boolean isExpiringWithin(Duration duration) {
        return expiryAt.isBefore(Instant.now().plus(duration));
    }

    public void updateToken(String newTokenValue, Instant newExpiryAt) {
        this.tokenValue = newTokenValue;
        this.expiryAt = newExpiryAt;
        this.lastUsedAt = Instant.now();
    }

    public void used() {
        this.lastUsedAt = Instant.now();
    }
}
