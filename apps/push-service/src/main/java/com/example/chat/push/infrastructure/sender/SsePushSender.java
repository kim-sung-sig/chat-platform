package com.example.chat.push.infrastructure.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SSE(Toast) 푸시 발송자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SsePushSender implements PushSender {
    private final SseSessionManager sseSessionManager;

    @Override
    public boolean support(String pushType) {
        return "TOAST".equalsIgnoreCase(pushType);
    }

    @Override
    public void send(String targetUserId, String title, String content) {
        log.info("Pushing TOAST message to user {} via SSE", targetUserId);
        sseSessionManager.send(targetUserId, title, content);
    }
}
