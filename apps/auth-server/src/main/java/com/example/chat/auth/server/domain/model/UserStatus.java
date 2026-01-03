package com.example.chat.auth.server.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
	ENABLED("계정 활성화"),
	LOCKED("계정 잠김"),
	EXPIRED("계정 만료"),
	DISABLED("계정 비활성화"),
	DELETED("계정 삭제");

	private final String key;

}
