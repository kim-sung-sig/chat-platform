package com.example.chat.common.core.exception;

import lombok.Getter;

/**
 * ê¸°ë³¸ ?ëŸ¬ ì½”ë“œ
 * ?œìŠ¤???„ë°˜?ì„œ ê³µí†µ?¼ë¡œ ?¬ìš©?˜ëŠ” ?ëŸ¬ ì½”ë“œ ?•ì˜
 */
@Getter
public enum BaseErrorCode implements ErrorCode {

    // 4xx Client Errors
    BAD_REQUEST("CMN-400", "?˜ëª»???”ì²­?…ë‹ˆ??, 400),
    UNAUTHORIZED("CMN-401", "?¸ì¦???„ìš”?©ë‹ˆ??, 401),
    FORBIDDEN("CMN-403", "?‘ê·¼ ê¶Œí•œ???†ìŠµ?ˆë‹¤", 403),
    NOT_FOUND("CMN-404", "ë¦¬ì†Œ?¤ë? ì°¾ì„ ???†ìŠµ?ˆë‹¤", 404),
    METHOD_NOT_ALLOWED("CMN-405", "?ˆìš©?˜ì? ?Šì? ë©”ì„œ?œì…?ˆë‹¤", 405),
    CONFLICT("CMN-409", "ë¦¬ì†Œ??ì¶©ëŒ??ë°œìƒ?ˆìŠµ?ˆë‹¤", 409),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR("CMN-500", "?œë²„ ?´ë? ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤", 500),
    SERVICE_UNAVAILABLE("CMN-503", "?œë¹„?¤ë? ?¬ìš©?????†ìŠµ?ˆë‹¤", 503),

    // Validation Errors
    VALIDATION_ERROR("CMN-VAL-001", "?…ë ¥ê°?ê²€ì¦ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤", 400),
    MISSING_PARAMETER("CMN-VAL-002", "?„ìˆ˜ ?Œë¼ë¯¸í„°ê°€ ?„ë½?˜ì—ˆ?µë‹ˆ??, 400),
    INVALID_FORMAT("CMN-VAL-003", "?¬ë°”ë¥´ì? ?Šì? ?•ì‹?…ë‹ˆ??, 400),

    // Business Logic Errors
    BUSINESS_LOGIC_ERROR("CMN-BIZ-001", "ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§ ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤", 400);

    private final String code;
    private final String message;
    private final int status;

    BaseErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
