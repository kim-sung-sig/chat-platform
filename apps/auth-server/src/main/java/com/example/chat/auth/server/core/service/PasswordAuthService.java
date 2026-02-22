package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 비밀번호 인증 서비스
 */
@Service
@RequiredArgsConstructor
public class PasswordAuthService {

    private final PasswordEncoder passwordEncoder;

    /** 비밀번호 검증 */
    public AuthResult authenticate(
            PasswordCredential storedCredential,
            PasswordCredential providedCredential,
            AuthenticationContext context) {
        boolean matches = passwordEncoder.matches(
                providedCredential.getPlainPassword(),
                storedCredential.getHashedPassword());

        if (!matches) {
            return AuthResult.failure("Invalid password");
        }

        return AuthResult.success(
                AuthLevel.LOW,
                Collections.singleton(CredentialType.PASSWORD));
    }

    /** 비밀번호 해싱 */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}
