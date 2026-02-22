package com.example.chat.domain.user;

/**
 * 사용자 상태
 */
public enum UserStatus {
    /**
     * 활성 상태
     */
    ACTIVE,

    /**
     * 정지 상태
     */
    SUSPENDED,

    /**
     * 차단 상태
     */
    BANNED,

    /**
     * 탈퇴 상태
     */
    DELETED
}
