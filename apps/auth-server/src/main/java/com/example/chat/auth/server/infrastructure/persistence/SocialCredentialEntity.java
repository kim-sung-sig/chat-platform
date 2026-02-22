package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("SOCIAL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class SocialCredentialEntity extends CredentialEntity {
    @Column(name = "provider")
    private String provider;

    @Column(name = "social_user_id")
    private String socialUserId;

    @Column(name = "email")
    private String email;

    @Override
    public Credential toDomain() {
        return new SocialCredential(provider, socialUserId, email, isVerified());
    }
}
