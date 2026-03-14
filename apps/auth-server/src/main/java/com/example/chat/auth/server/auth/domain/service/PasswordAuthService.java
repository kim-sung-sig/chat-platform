package com.example.chat.auth.server.auth.domain.service;

import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthResult;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.auth.domain.CredentialType;
import com.example.chat.auth.server.auth.domain.credential.PasswordCredential;
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

    /** 평문 비밀번호와 해시 일치 여부 */
    public boolean matches(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
