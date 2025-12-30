package com.example.chat.common.auth.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

/**
 * 인증된 사용자 정보
 * Spring Security Principal로 사용
 */
@Getter
@Builder
public class AuthUser implements Serializable {

    private final UserId userId;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final Set<String> authorities;

    public Long getUserIdValue() {
        return userId != null ? userId.getValue() : null;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasAuthority(String authority) {
        return authorities != null && authorities.contains(authority);
    }
}
