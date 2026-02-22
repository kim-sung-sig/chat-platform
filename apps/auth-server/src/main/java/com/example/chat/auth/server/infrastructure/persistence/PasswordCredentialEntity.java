package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@DiscriminatorValue("PASSWORD")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PasswordCredentialEntity extends CredentialEntity {
    @Column(name = "hashed_password")
    private String hashedPassword;

    @Override
    public Credential toDomain() {
        return new PasswordCredential(hashedPassword, isVerified());
    }
}
