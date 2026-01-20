package com.example.chat.auth.server.core.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.chat.auth.server.core.domain.credential.Device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@Builder
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
        this.tokenValue = Objects.requireNonNull(newTokenValue);
        this.expiryAt = Objects.requireNonNull(newExpiryAt);
        this.lastUsedAt = Instant.now();
    }

    public void used() {
        this.lastUsedAt = Instant.now();
    }

}
