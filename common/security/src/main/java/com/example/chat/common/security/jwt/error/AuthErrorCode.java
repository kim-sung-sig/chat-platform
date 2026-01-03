package com.example.chat.common.security.jwt.error;

import com.example.chat.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_TOKEN("AUTH_001", "? íš¨?˜ì? ?Šì? ? í°", 401),
	EXPIRED_TOKEN("AUTH_002", "ë§Œë£Œ??? í°", 401),
	INSUFFICIENT_SCOPE("AUTH_003", "ê¶Œí•œ??ë¶€ì¡±í•©?ˆë‹¤", 403),
	;

	private final String code;
	private final String message;
	private final int status;

}
