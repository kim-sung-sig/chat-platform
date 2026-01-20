package com.example.chat.auth.server.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import com.example.chat.auth.server.core.domain.RefreshToken;
import com.example.chat.auth.server.core.domain.credential.Device;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    private UUID id;

    @Column(name = "principal_id", nullable = false)
    private UUID principalId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "deviceId", column = @Column(name = "device_id")),
            @AttributeOverride(name = "platform", column = @Column(name = "platform")),
            @AttributeOverride(name = "browser", column = @Column(name = "browser"))
    })
    private Device device;

    @Column(name = "token_value", length = 1024, nullable = false, unique = true)
    private String tokenValue;

    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public static RefreshTokenEntity fromDomain(RefreshToken domain) {
        return RefreshTokenEntity.builder()
                .id(domain.getId())
                .principalId(domain.getPrincipalId())
                .device(domain.getDevice())
                .tokenValue(domain.getTokenValue())
                .expiryAt(domain.getExpiryAt())
                .createdAt(domain.getCreatedAt())
                .lastUsedAt(domain.getLastUsedAt())
                .build();
    }

    public RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(this.id)
                .principalId(this.principalId)
                .device(this.device)
                .tokenValue(this.tokenValue)
                .expiryAt(this.expiryAt)
                .createdAt(this.createdAt)
                .lastUsedAt(this.lastUsedAt)
                .build();
    }
}
