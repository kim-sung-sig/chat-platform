package com.example.chat.auth.server.signup.application;

import com.example.chat.auth.server.rest.dto.request.SignupRequest;
import com.example.chat.auth.server.rest.dto.response.SignupResponse;
import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.Principal;
import com.example.chat.auth.server.auth.domain.PrincipalType;
import com.example.chat.auth.server.auth.domain.credential.PasswordCredential;
import com.example.chat.auth.server.profile.domain.UserProfile;
import com.example.chat.auth.server.auth.domain.repository.CredentialRepository;
import com.example.chat.auth.server.auth.domain.repository.PrincipalRepository;
import com.example.chat.auth.server.profile.domain.repository.UserProfileRepository;
import com.example.chat.auth.server.auth.domain.service.PasswordAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원가입 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignupApplicationService {

    private final PrincipalRepository principalRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordAuthService passwordAuthService;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("Signup request for identifier: {}", request.email());

        // 1. 비밀번호 길이 검증
        if (request.password().length() < 8) {
            throw new AuthException(AuthServerErrorCode.WEAK_PASSWORD);
        }

        // 2. 중복 이메일 확인
        if (principalRepository.findByIdentifier(request.email()).isPresent()) {
            throw new AuthException(AuthServerErrorCode.DUPLICATE_IDENTIFIER);
        }

        // 3. Principal 생성 (identifier = email)
        Principal principal = Principal.builder()
                .id(UUID.randomUUID())
                .identifier(request.email())
                .type(PrincipalType.USER)
                .active(true)
                .build();
        principalRepository.save(principal);

        // 4. 비밀번호 해싱 후 Credential 저장
        String hashedPassword = passwordAuthService.hashPassword(request.password());
        PasswordCredential credential = new PasswordCredential(hashedPassword, true);
        credentialRepository.save(principal.getId(), credential);

        // 5. 기본 UserProfile 생성 (nickname = 요청값 또는 이메일 앞부분)
        String nickname = request.nickname() != null && !request.nickname().isBlank()
                ? request.nickname()
                : request.email().split("@")[0];
        UserProfile profile = UserProfile.create(principal.getId(), nickname);
        userProfileRepository.save(profile);

        log.info("Signup completed for principal: {}", principal.getId());

        return new SignupResponse(
                principal.getId(),
                principal.getIdentifier());
    }
}
