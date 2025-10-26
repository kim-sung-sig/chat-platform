package com.example.chat.system.controller;

import com.example.chat.system.dto.request.MessageCreateRequest;
import com.example.chat.system.dto.request.MessageUpdateRequest;
import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.MessageResponse;
import com.example.chat.system.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 메시지 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("POST /api/v1/messages - Creating message");
        MessageResponse response = messageService.createMessage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("메시지가 생성되었습니다", response));
    }

    /**
     * 메시지 조회
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(
            @PathVariable Long messageId) {
        log.info("GET /api/v1/messages/{}", messageId);
        MessageResponse response = messageService.getMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 채널별 메시지 목록 조회
     */
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessagesByChannel(
            @PathVariable Long channelId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/v1/messages/channel/{}", channelId);
        Page<MessageResponse> response = messageService.getMessagesByChannel(channelId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 메시지 수정
     */
    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> updateMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageUpdateRequest request) {
        log.info("PUT /api/v1/messages/{}", messageId);
        MessageResponse response = messageService.updateMessage(messageId, request);
        return ResponseEntity.ok(ApiResponse.success("메시지가 수정되었습니다", response));
    }

    /**
     * 메시지 취소
     */
    @PostMapping("/{messageId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelMessage(@PathVariable Long messageId) {
        log.info("POST /api/v1/messages/{}/cancel", messageId);
        messageService.cancelMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("메시지가 취소되었습니다", null));
    }

    /**
     * 메시지 삭제
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        log.info("DELETE /api/v1/messages/{}", messageId);
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("메시지가 삭제되었습니다", null));
    }
}