package com.example.chat.domain.message

/**
 * 메시지 상태
 */
enum class MessageStatus {
    /**
     * 대기 중 (아직 발송되지 않음)
     */
    PENDING,

    /**
     * 발송됨 (서버에서 전송 완료)
     */
    SENT,

    /**
     * 전달됨 (수신자에게 도달)
     */
    DELIVERED,

    /**
     * 읽음 (수신자가 읽음)
     */
    READ,

    /**
     * 실패 (발송 실패)
     */
    FAILED
}

