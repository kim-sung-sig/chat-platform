package com.example.chat.auth.server.infrastructure.persistence;

import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("OTP")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class OtpCredentialEntity extends CredentialEntity {
    @Column(name = "otp_code")
    private String code;

    @Column(name = "delivery_method")
    private String deliveryMethod;

    @Override
    public Credential toDomain() {
        return new OtpCredential(code, deliveryMethod);
    }
}
