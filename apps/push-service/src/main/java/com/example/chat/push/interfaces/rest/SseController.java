package com.example.chat.push.interfaces.rest;

import com.example.chat.push.infrastructure.sender.SseSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 스트림 컨트롤러
 *
 * 책임: 클라이언트에게 SSE 연결을 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class SseController {
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    private final SseSessionManager sseSessionManager;

    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String userId) throws Exception {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.send(SseEmitter.event().name("connect").data("Connected for user: " + userId));
        sseSessionManager.add(userId, emitter);
        return emitter;
    }
}
