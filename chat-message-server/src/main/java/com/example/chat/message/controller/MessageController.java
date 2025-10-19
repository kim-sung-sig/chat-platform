package com.example.chat.message.controller;

import com.example.chat.message.api.request.SendMessageRequest;
import com.example.chat.message.usecase.MessageAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 간단한 HTTP API(스켈레톤). 실서비스는 WebSocket을 메인 라우트로 사용하되 이 REST는 관리/조회용으로 사용합니다.
 */
@RestController
@RequestMapping("/api/channels")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageAppService messageAppService;

    public MessageController(MessageAppService messageAppService) {
        this.messageAppService = messageAppService;
    }

    @PostMapping("/{channelId}/messages")
    public ResponseEntity<Void> sendMessage(@PathVariable String channelId, @RequestBody SendMessageRequest request) {
        // TODO: 인증/권한 검증 (JWT 등)
        messageAppService.sendMessage(channelId, request);
        logger.info("HTTP sendMessage accepted channel={} sender={}", channelId, request.getSenderId());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{channelId}/messages")
    public ResponseEntity<MessagesResponse> fetchMessages(@PathVariable String channelId,
                                                           @RequestParam(required = false) String cursor,
                                                           @RequestParam(defaultValue = "50") int limit) {
        MessagesResponse resp = messageAppService.fetchMessages(channelId, cursor, limit);
        return ResponseEntity.ok(resp);
    }
}