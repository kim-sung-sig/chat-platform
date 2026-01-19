package com.example.chat.auth.server.core.domain.credential;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;

/**
 * OTP 자격증명
 * - SMS, Email, Authenticator App으로 전달되는 일회용 코드
 * - 주로 MFA에서 사용
 */
public class OtpCredential extends Credential {

    private final String code;
    private final String deliveryMethod;  // SMS, EMAIL, APP

    public OtpCredential(String code, String deliveryMethod) {
        super(CredentialType.OTP, false);  // OTP는 생성 시 미검증
        this.code = code;
        this.deliveryMethod = deliveryMethod;
    }

    public String getCode() {
        return code;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    @Override
    public AuthLevel minAuthLevel() {
        // OTP는 단독으로는 LOW, 하지만 조합하면 MEDIUM
        return AuthLevel.LOW;
    }
}
