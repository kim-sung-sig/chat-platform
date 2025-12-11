package com.example.chat.domain.user;

/**
 * 사용자 상태 Enum
 */
public enum UserStatus {
    /**
     * 활성 상태 (정상)
     */
    ACTIVE,

    /**
     * 정지 상태 (일시적)
     */
    SUSPENDED,

    /**
     * 차단 상태 (영구적)
     */
    BANNED,

    /**
     * 탈퇴 상태
     */
    WITHDRAWN
}
