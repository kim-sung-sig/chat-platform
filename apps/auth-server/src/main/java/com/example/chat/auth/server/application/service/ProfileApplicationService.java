package com.example.chat.auth.server.application.service;

import com.example.chat.auth.server.api.dto.request.ChangePasswordRequest;
import com.example.chat.auth.server.api.dto.request.UpdateProfileRequest;
import com.example.chat.auth.server.api.dto.response.ProfileResponse;
import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.profile.UserProfile;
import com.example.chat.auth.server.core.repository.CredentialRepository;
import com.example.chat.auth.server.core.repository.UserProfileRepository;
import com.example.chat.auth.server.core.service.PasswordAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 프로필 관리 Application Service
 * 프로필 조회, 수정, 비밀번호 변경을 오케스트레이션한다.
 * 비즈니스 로직은 도메인 객체(UserProfile) 내부에 위임한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileApplicationService {

    private final UserProfileRepository userProfileRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordAuthService passwordAuthService;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID principalId) {
        UserProfile profile = userProfileRepository.findByPrincipalId(principalId)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.PROFILE_NOT_FOUND));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID principalId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByPrincipalId(principalId)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.PROFILE_NOT_FOUND));

        if (request.nickname() != null) profile.changeNickname(request.nickname());
        if (request.avatarUrl() != null) profile.changeAvatarUrl(request.avatarUrl());
        if (request.phoneNumber() != null) profile.registerPhoneNumber(request.phoneNumber());
        if (request.bio() != null) profile.updateBio(request.bio());

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile updated for principal: {}", principalId);
        return ProfileResponse.from(saved);
    }

    @Transactional
    public void changePassword(UUID principalId, ChangePasswordRequest request) {
        PasswordCredential stored = (PasswordCredential) credentialRepository
                .findByPrincipalId(principalId, CredentialType.PASSWORD)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS));

        if (!passwordAuthService.matches(request.currentPassword(), stored.getHashedPassword())) {
            throw new AuthException(AuthServerErrorCode.INVALID_CURRENT_PASSWORD);
        }
        if (passwordAuthService.matches(request.newPassword(), stored.getHashedPassword())) {
            throw new AuthException(AuthServerErrorCode.SAME_PASSWORD);
        }

        String newHash = passwordAuthService.hashPassword(request.newPassword());
        credentialRepository.save(principalId, new PasswordCredential(newHash, true));
        log.info("Password changed for principal: {}", principalId);
    }
}
