package com.example.chat.auth.server.core.domain.credential;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.CredentialType;

/**
 * 비밀번호 자격증명
 */
public class PasswordCredential extends Credential {

    private final String hashedPassword;
    private final String plainPassword;  // 검증 중에만 임시 보관

    public PasswordCredential(String hashedPassword, boolean verified) {
        super(CredentialType.PASSWORD, verified);
        this.hashedPassword = hashedPassword;
        this.plainPassword = null;
    }

    // 검증 중에 사용 (임시)
    public PasswordCredential(String hashedPassword, String plainPassword) {
        super(CredentialType.PASSWORD, false);
        this.hashedPassword = hashedPassword;
        this.plainPassword = plainPassword;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    @Override
    public AuthLevel minAuthLevel() {
        return AuthLevel.LOW;
    }
}
