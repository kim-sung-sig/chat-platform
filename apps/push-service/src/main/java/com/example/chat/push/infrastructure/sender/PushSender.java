package com.example.chat.push.infrastructure.sender;

/**
 * 푸시 발송 인터페이스
 *
 * 다형성 기반 설계 - 타입별로 구현체가 분리됩니다.
 */
public interface PushSender {
    boolean support(String pushType);

    void send(String targetUserId, String title, String content);
}
