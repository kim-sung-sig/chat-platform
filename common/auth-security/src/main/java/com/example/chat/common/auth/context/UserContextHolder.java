package com.example.chat.common.auth.context;

import com.example.chat.common.auth.model.AuthUser;
import com.example.chat.common.auth.model.UserId;

/**
 * 사용자 컨텍스트 홀더
 * ThreadLocal을 사용하여 현재 스레드의 사용자 정보 관리
 */
public final class UserContextHolder {

    private static final ThreadLocal<AuthUser> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * 현재 스레드의 사용자 정보 설정
     */
    public static void setUser(AuthUser user) {
        CONTEXT.set(user);
    }

    /**
     * 현재 스레드의 사용자 정보 조회
     */
    public static AuthUser getUser() {
        return CONTEXT.get();
    }

    /**
     * 현재 스레드의 사용자 ID 조회
     */
    public static UserId getUserId() {
        AuthUser user = CONTEXT.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 현재 스레드의 사용자 ID 값 조회
     */
    public static Long getUserIdValue() {
        AuthUser user = CONTEXT.get();
        return user != null ? user.getUserIdValue() : null;
    }

    /**
     * 현재 스레드의 사용자 정보 제거
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
