package com.example.chat.common.security.core.util;

import com.example.chat.common.security.core.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Security 유틸리티
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 현재 인증된 사용자 정보 조회
     */
    public static Optional<AuthenticatedUser> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(AuthenticatedUser::from);
    }

    /**
     * 현재 사용자 ID 조회
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentUser().map(AuthenticatedUser::getUserId);
    }

    /**
     * 현재 사용자가 특정 역할을 가지고 있는지 확인
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    public static boolean isAdmin() {
        return getCurrentUser()
                .map(AuthenticatedUser::isAdmin)
                .orElse(false);
    }
}

