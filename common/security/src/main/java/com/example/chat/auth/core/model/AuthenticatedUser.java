package com.example.chat.auth.core.model;

import java.util.Collections;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 인증된 사용자 정보
 */
public record AuthenticatedUser(
        String userId,
        String email,
        List<String> roles) {
    public AuthenticatedUser {
        if (roles == null)
            roles = Collections.emptyList();
    }

    /**
     * JWT로부터 AuthenticatedUser 생성
     */
    public static AuthenticatedUser from(Jwt jwt) {
        if (jwt == null)
            return null;

        List<String> roles = jwt.getClaimAsStringList("roles");
        return new AuthenticatedUser(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                roles != null ? roles : Collections.emptyList());
    }

    /**
     * 특정 역할을 가지고 있는지 확인
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
