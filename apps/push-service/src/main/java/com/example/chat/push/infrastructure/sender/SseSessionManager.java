package com.example.chat.push.infrastructure.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 세션 관리자
 *
 * 책임: SSE 연결 관리 (추가/제거/전송)
 */
@Component
@Slf4j
public class SseSessionManager {
    private final ConcurrentHashMap<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void add(String userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        log.info("Added SSE emitter for user {}. Active users: {}", userId, emitters.size());
    }

    public void remove(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    public void send(String userId, String title, String content) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("push")
                        .data(Map.of("title", title, "content", content)));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }

        deadEmitters.forEach(emitter -> remove(userId, emitter));
    }
}
