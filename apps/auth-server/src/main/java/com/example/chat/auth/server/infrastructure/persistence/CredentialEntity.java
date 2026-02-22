package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credentials")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "credential_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public abstract class CredentialEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "principal_id", nullable = false)
    private UUID principalId;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "credential_type", insertable = false, updatable = false)
    private String type;

    public abstract Credential toDomain();

    public static CredentialEntity fromDomain(UUID principalId, Credential domain) {
        if (domain instanceof PasswordCredential d) {
            return PasswordCredentialEntity.builder()
                    .principalId(principalId)
                    .hashedPassword(d.getHashedPassword() != null ? d.getHashedPassword() : "")
                    .verified(d.isVerified())
                    .build();
        } else if (domain instanceof SocialCredential d) {
            return SocialCredentialEntity.builder()
                    .principalId(principalId)
                    .provider(d.getProvider())
                    .socialUserId(d.getSocialUserId())
                    .email(d.getEmail())
                    .verified(d.isVerified())
                    .build();
        } else if (domain instanceof PasskeyCredential d) {
            return PasskeyCredentialEntity.builder()
                    .principalId(principalId)
                    .credentialId(d.getCredentialId())
                    .publicKey(d.getPublicKey())
                    .authenticatorName(d.getAuthenticatorName())
                    .verified(d.isVerified())
                    .build();
        } else if (domain instanceof OtpCredential d) {
            return OtpCredentialEntity.builder()
                    .principalId(principalId)
                    .code(d.getCode())
                    .deliveryMethod(d.getDeliveryMethod())
                    .verified(false)
                    .build();
        }
        throw new IllegalArgumentException("Unsupported credential type: " + domain.getType());
    }
}
