package com.example.chat.auth.server.core.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;

/**
 * 비밀번호 인증 서비스
 * - 평문 비밀번호를 해시된 비밀번호와 검증
 * - "어떻게 검증할 것인가"는 서비스의 책임
 * - 도메인(정책)은 "검증 성공했으면 LOW 수준"만 관심
 */
@Service
public class PasswordAuthService {

    private final PasswordEncoder passwordEncoder;

    public PasswordAuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 비밀번호 검증
     */
    public AuthResult authenticate(PasswordCredential storedCredential,
                                   PasswordCredential providedCredential,
                                   AuthenticationContext context) {
        // 평문 비밀번호 검증
        boolean matches = passwordEncoder.matches(
                providedCredential.getPlainPassword(),
                storedCredential.getHashedPassword()
        );

        if (!matches) {
            return AuthResult.failure("Invalid password");
        }

        // 성공 → LOW 수준의 인증 달성
        Set<CredentialType> completed = new HashSet<>();
        completed.add(CredentialType.PASSWORD);

        return AuthResult.success(AuthLevel.LOW, completed);
    }

    /**
     * 비밀번호 해싱
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}
