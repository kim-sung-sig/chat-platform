package com.example.chat.auth.server.core.domain;

/**
 * 자격 증명의 추상 개념
 * - 비밀번호, 소셜 계정, 패스키 등 모든 인증 방식의 상위 개념
 * - 실제 검증은 서비스 계층에서 수행
 * - 도메인은 "이런 타입의 자격증명이 있다"만 관심
 */
public abstract class Credential {
    private final CredentialType type;
    private final boolean verified;

    protected Credential(CredentialType type, boolean verified) {
        this.type = type;
        this.verified = verified;
    }

    public CredentialType getType() {
        return type;
    }

    public boolean isVerified() {
        return verified;
    }

    /**
     * 이 자격증명이 달성 가능한 최소 인증 수준
     */
    public abstract AuthLevel minAuthLevel();
}
