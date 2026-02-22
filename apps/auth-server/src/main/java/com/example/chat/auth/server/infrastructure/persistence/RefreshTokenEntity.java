package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.RefreshToken;
import com.example.chat.auth.server.core.domain.credential.Device;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    public RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(id)
                .principalId(principalId)
                .device(device)
                .tokenValue(tokenValue)
                .expiryAt(expiryAt)
                .createdAt(createdAt)
                .lastUsedAt(lastUsedAt != null ? lastUsedAt : createdAt)
                .build();
    }

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
}
