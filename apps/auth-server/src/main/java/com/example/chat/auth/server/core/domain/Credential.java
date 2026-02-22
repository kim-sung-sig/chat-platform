package com.example.chat.auth.server.core.domain;

import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자격 증명의 추상 개념
 */
@Getter
@RequiredArgsConstructor
public abstract sealed class Credential permits PasswordCredential, SocialCredential, PasskeyCredential, OtpCredential {
    private final CredentialType type;
    private final boolean verified;

    /** 이 자격증명이 달성 가능한 최소 인증 수준 */
    public abstract AuthLevel minAuthLevel();
}
