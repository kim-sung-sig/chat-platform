package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.core.domain.AuthLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 작업별 필요한 인증 수준
 */
@Getter
@RequiredArgsConstructor
public enum OperationAuthLevel {
    // 조회 작업
    VIEW_PROFILE(AuthLevel.LOW, "프로필 조회"),
    VIEW_MESSAGES(AuthLevel.LOW, "메시지 조회"),

    // 수정 작업
    UPDATE_PROFILE(AuthLevel.MEDIUM, "프로필 수정"),
    CHANGE_PASSWORD(AuthLevel.MEDIUM, "비밀번호 변경"),

    // 민감한 작업
    PAYMENT(AuthLevel.HIGH, "결제"),
    TRANSFER_MONEY(AuthLevel.HIGH, "송금"),
    DELETE_ACCOUNT(AuthLevel.HIGH, "계정 삭제"),

    // 관리자 작업
    ADMIN_ACCESS(AuthLevel.HIGH, "관리자 권한");

    private final AuthLevel requiredLevel;
    private final String description;

    /**
     * 현재 인증 수준으로 이 작업을 수행할 수 있는가?
     */
    public boolean canPerform(AuthLevel currentLevel) {
        return currentLevel.isHigherOrEqual(requiredLevel);
    }

    /**
     * 이 작업을 수행하기 위해 필요한 추가 인증 수준
     */
    public AuthLevel requiredUpgrade(AuthLevel currentLevel) {
        if (canPerform(currentLevel)) {
            return null;
        }
        return requiredLevel;
    }
}
