package com.example.chat.message.controller;

import com.example.chat.common.dto.UserId;
import com.example.chat.common.port.MessageReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageReadController {

    private final MessageReadRepository messageReadRepository;

    public MessageReadController(MessageReadRepository messageReadRepository) {
        this.messageReadRepository = messageReadRepository;
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<?> markRead(@PathVariable Long messageId, @RequestParam UserId userId) {
        boolean created = messageReadRepository.markRead(messageId, userId);
        return ResponseEntity.ok().body(created ? "marked" : "already_read");
    }

    @GetMapping("/{messageId}/readers")
    public ResponseEntity<List<Long>> getReaders(@PathVariable Long messageId) {
        List<Long> readers = messageReadRepository.findReaders(messageId);
        return ResponseEntity.ok(readers);
    }

    @GetMapping("/{messageId}/isRead")
    public ResponseEntity<Boolean> isRead(@PathVariable Long messageId, @RequestParam UserId userId) {
        boolean read = messageReadRepository.isReadBy(messageId, userId);
        return ResponseEntity.ok(read);
    }
}