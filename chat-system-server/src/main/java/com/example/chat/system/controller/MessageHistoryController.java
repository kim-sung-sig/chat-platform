package com.example.chat.system.controller;

import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.CursorPageResponse;
import com.example.chat.system.dto.response.MessageHistoryResponse;
import com.example.chat.system.service.MessageHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메시지 발행 이력 API 컨트롤러
 * 커서 기반 페이징 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/message-histories")
@RequiredArgsConstructor
public class MessageHistoryController {

    private final MessageHistoryService messageHistoryService;

    /**
     * 메시지별 발행 이력 조회 (커서 기반 페이징)
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<ApiResponse<CursorPageResponse<MessageHistoryResponse>>> getHistoriesByMessage(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("GET /api/v1/message-histories/message/{} - cursor: {}, size: {}", messageId, cursor, size);

        CursorPageResponse<MessageHistoryResponse> response =
                messageHistoryService.getHistoriesByMessage(messageId, cursor, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 고객별 발행 이력 조회 (커서 기반 페이징)
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CursorPageResponse<MessageHistoryResponse>>> getHistoriesByCustomer(
            @PathVariable Long customerId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("GET /api/v1/message-histories/customer/{} - cursor: {}, size: {}", customerId, cursor, size);

        CursorPageResponse<MessageHistoryResponse> response =
                messageHistoryService.getHistoriesByCustomer(customerId, cursor, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스케줄별 발행 이력 조회 (커서 기반 페이징)
     */
    @GetMapping("/schedule/{scheduleRuleId}")
    public ResponseEntity<ApiResponse<CursorPageResponse<MessageHistoryResponse>>> getHistoriesBySchedule(
            @PathVariable Long scheduleRuleId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("GET /api/v1/message-histories/schedule/{} - cursor: {}, size: {}", scheduleRuleId, cursor, size);

        CursorPageResponse<MessageHistoryResponse> response =
                messageHistoryService.getHistoriesByScheduleRule(scheduleRuleId, cursor, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}