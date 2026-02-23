package com.example.chat.auth.server.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자격 증명의 추상 개념
 *
 * 구현체: PasswordCredential, SocialCredential, PasskeyCredential, OtpCredential
 */
@Getter
@RequiredArgsConstructor
public abstract class Credential {
    private final CredentialType type;
    private final boolean verified;

    /** 이 자격증명이 달성 가능한 최소 인증 수준 */
    public abstract AuthLevel minAuthLevel();
}
