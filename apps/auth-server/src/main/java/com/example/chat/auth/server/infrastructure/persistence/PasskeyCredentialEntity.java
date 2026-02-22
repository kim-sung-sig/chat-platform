package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("PASSKEY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PasskeyCredentialEntity extends CredentialEntity {
    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "public_key", length = 1024)
    private String publicKey;

    @Column(name = "authenticator_name")
    private String authenticatorName;

    @Override
    public Credential toDomain() {
        return new PasskeyCredential(credentialId, publicKey, authenticatorName, isVerified());
    }
}
