package com.example.chat.auth.server.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
	ROLE_USER("ROLE_USER", "사용자"),
	ROLE_ADMIN("ROLE_ADMIN", "관리자");

	private final String key;
	private final String name;

}
