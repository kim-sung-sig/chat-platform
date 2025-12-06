package com.example.chat.storage.domain.message;

/**
 * 메시지 상태 Enum
 */
public enum MessageStatus {
    /**
     * 대기 중 (발송 전)
     */
    PENDING("pending", "대기 중"),

    /**
     * 발송됨
     */
    SENT("sent", "발송됨"),

    /**
     * 전달됨 (서버가 수신함)
     */
    DELIVERED("delivered", "전달됨"),

    /**
     * 읽음
     */
    READ("read", "읽음"),

    /**
     * 실패
     */
    FAILED("failed", "실패"),

    /**
     * 취소됨
     */
    CANCELLED("cancelled", "취소됨");

    private final String code;
    private final String description;

    MessageStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MessageStatus fromCode(String code) {
        for (MessageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown message status code: " + code);
    }

    /**
     * 읽음 상태로 전환 가능한지 확인
     */
    public boolean canTransitionToRead() {
        return this == SENT || this == DELIVERED;
    }

    /**
     * 실패 상태로 전환 가능한지 확인
     */
    public boolean canTransitionToFailed() {
        return this == PENDING || this == SENT;
    }
}
