package com.example.chat.auth.core.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * 인증된 사용자 정보
 */
@Getter
@Builder
public class AuthenticatedUser {

	private final String userId;
	private final String email;
	private final List<String> roles;

	/**
	 * JWT로부터 AuthenticatedUser 생성
	 */
	public static AuthenticatedUser from(Jwt jwt) {
		if (jwt == null) {
			return null;
		}

		return AuthenticatedUser.builder()
				.userId(jwt.getSubject())
				.email(jwt.getClaimAsString("email"))
				.roles(jwt.getClaimAsStringList("roles"))
				.build();
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

