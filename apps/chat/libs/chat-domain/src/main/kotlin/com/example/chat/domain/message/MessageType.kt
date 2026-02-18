package com.example.chat.domain.message

/**
 * 메시지 타입
 */
enum class MessageType {
    /**
     * 일반 텍스트 메시지
     */
    TEXT,

    /**
     * 이미지 메시지
     */
    IMAGE,

    /**
     * 파일 메시지
     */
    FILE,

    /**
     * 시스템 메시지 (입장, 퇴장 등)
     */
    SYSTEM,

    /**
     * 비디오 메시지
     */
    VIDEO,

    /**
     * 오디오 메시지
     */
    AUDIO
}

