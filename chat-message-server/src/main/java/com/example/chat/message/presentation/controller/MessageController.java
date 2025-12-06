package com.example.chat.message.presentation.controller;

import com.example.chat.message.application.dto.request.SendMessageRequest;
import com.example.chat.message.application.dto.response.MessageResponse;
import com.example.chat.message.application.service.MessageApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService messageApplicationService;

    /**
     * 메시지 발송
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {
        log.info("POST /api/messages - roomId: {}, type: {}",
            request.getRoomId(), request.getMessageType());

        MessageResponse response = messageApplicationService.sendMessage(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 답장 메시지 발송
     */
    @PostMapping("/reply")
    public ResponseEntity<MessageResponse> sendReplyMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {
        log.info("POST /api/messages/reply - roomId: {}, replyTo: {}",
            request.getRoomId(), request.getReplyToMessageId());

        MessageResponse response = messageApplicationService.sendReplyMessage(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
