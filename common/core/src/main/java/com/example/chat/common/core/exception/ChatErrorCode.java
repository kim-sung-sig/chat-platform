package com.example.chat.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chat 도메인 에러 코드
 */
@Getter
@AllArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    // Channel
    CHANNEL_NOT_FOUND("CHAT-CH-001", "채널을 찾을 수 없습니다", 404),
    CHANNEL_NOT_ACTIVE("CHAT-CH-002", "비활성화된 채널입니다", 400),
    CHANNEL_NOT_MEMBER("CHAT-CH-003", "채널 멤버가 아닙니다", 403),
    CHANNEL_ALREADY_MEMBER("CHAT-CH-004", "이미 채널 멤버입니다", 409),

    // Message
    MESSAGE_NOT_FOUND("CHAT-MSG-001", "메시지를 찾을 수 없습니다", 404),
    MESSAGE_SEND_FORBIDDEN("CHAT-MSG-002", "메시지를 전송할 권한이 없습니다", 403),

    // Friendship
    FRIENDSHIP_NOT_FOUND("CHAT-FR-001", "친구 관계를 찾을 수 없습니다", 404),
    FRIENDSHIP_ALREADY_EXISTS("CHAT-FR-002", "이미 친구 요청을 보냈습니다", 409),
    FRIENDSHIP_ALREADY_FRIENDS("CHAT-FR-003", "이미 친구 관계입니다", 409),
    FRIENDSHIP_BLOCKED("CHAT-FR-004", "차단된 사용자입니다", 403),

    // User
    USER_NOT_FOUND("CHAT-USR-001", "사용자를 찾을 수 없습니다", 404),
    USER_NOT_ACTIVE("CHAT-USR-002", "비활성 사용자입니다", 403),

    // Domain Rule
    DOMAIN_RULE_VIOLATION("CHAT-DOM-001", "도메인 규칙 위반입니다", 400);

    private final String code;
    private final String message;
    private final int status;
}
