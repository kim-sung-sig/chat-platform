package com.example.chat.storage.domain.message;

/**
 * 메시지 타입 Enum
 * 확장 가능하도록 설계
 */
public enum MessageType {
    /**
     * 텍스트 메시지
     */
    TEXT("text", "텍스트 메시지"),

    /**
     * 이미지 메시지
     */
    IMAGE("image", "이미지 메시지"),

    /**
     * 파일 메시지
     */
    FILE("file", "파일 메시지"),

    /**
     * 비디오 메시지
     */
    VIDEO("video", "비디오 메시지"),

    /**
     * 오디오 메시지
     */
    AUDIO("audio", "오디오 메시지"),

    /**
     * 시스템 알림 메시지
     */
    SYSTEM("system", "시스템 알림"),

    /**
     * 예약 메시지
     */
    SCHEDULED("scheduled", "예약 메시지"),

    /**
     * 봇 메시지
     */
    BOT("bot", "봇 메시지"),

    /**
     * 리치 콘텐츠 (카드, 버튼 등)
     */
    RICH_CONTENT("rich_content", "리치 콘텐츠"),

    /**
     * 위치 메시지
     */
    LOCATION("location", "위치 메시지"),

    /**
     * 스티커 메시지
     */
    STICKER("sticker", "스티커 메시지");

    private final String code;
    private final String description;

    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}
