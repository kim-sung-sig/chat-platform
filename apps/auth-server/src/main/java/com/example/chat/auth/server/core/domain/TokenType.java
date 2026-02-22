package com.example.chat.auth.server.core.domain;

/**
 * 토큰 타입
 */
public enum TokenType {
    /**
     * 완전한 인증 토큰
     * - 모든 API 접근 가능
     * - AuthLevel에 따라 권한 차등
     */
    FULL_ACCESS,

    /**
     * MFA 대기 토큰
     * - MFA 완료 API만 접근 가능
     * - 임시 토큰 (짧은 TTL)
     */
    MFA_PENDING
}
