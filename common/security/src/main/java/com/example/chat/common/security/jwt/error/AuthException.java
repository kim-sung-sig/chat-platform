package com.example.chat.common.security.jwt.error;

import com.example.chat.common.core.exception.BaseException;

/**
 * ?¸ì¦/?¸ê? ê´€??ë¹„ì¦ˆ?ˆìŠ¤ ?ˆì™¸??ê¸°ë³¸ ?´ë˜??
 * ?„ë¡œ?íŠ¸??ê³µí†µ BaseException???ì†?˜ì—¬ ErrorResponse ë³€?˜ì„ ?¼ê??”í•©?ˆë‹¤.
 */
public abstract class AuthException extends BaseException {

	protected AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}

}
