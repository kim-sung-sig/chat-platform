package com.example.chat.system.controller;

import com.example.chat.system.dto.request.ScheduleCreateRequest;
import com.example.chat.system.dto.response.ApiResponse;
import com.example.chat.system.dto.response.ScheduleRuleResponse;
import com.example.chat.system.service.ScheduleRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 스케줄 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRuleService scheduleRuleService;

    /**
     * 스케줄 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleRuleResponse>> createSchedule(
            @Valid @RequestBody ScheduleCreateRequest request) {
        log.info("POST /api/v1/schedules - Creating schedule for message: {}", request.getMessageId());
        ScheduleRuleResponse response = scheduleRuleService.createSchedule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스케줄이 생성되었습니다", response));
    }

    /**
     * 스케줄 조회
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleRuleResponse>> getSchedule(
            @PathVariable Long scheduleId) {
        log.info("GET /api/v1/schedules/{}", scheduleId);
        ScheduleRuleResponse response = scheduleRuleService.getSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 메시지별 스케줄 목록 조회
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<ApiResponse<List<ScheduleRuleResponse>>> getSchedulesByMessage(
            @PathVariable Long messageId) {
        log.info("GET /api/v1/schedules/message/{}", messageId);
        List<ScheduleRuleResponse> response = scheduleRuleService.getSchedulesByMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 활성화된 스케줄 목록 조회
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ScheduleRuleResponse>>> getActiveSchedules() {
        log.info("GET /api/v1/schedules/active");
        List<ScheduleRuleResponse> response = scheduleRuleService.getActiveSchedules();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스케줄 활성화
     */
    @PostMapping("/{scheduleId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateSchedule(@PathVariable Long scheduleId) {
        log.info("POST /api/v1/schedules/{}/activate", scheduleId);
        scheduleRuleService.activateSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("스케줄이 활성화되었습니다", null));
    }

    /**
     * 스케줄 비활성화
     */
    @PostMapping("/{scheduleId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateSchedule(@PathVariable Long scheduleId) {
        log.info("POST /api/v1/schedules/{}/deactivate", scheduleId);
        scheduleRuleService.deactivateSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("스케줄이 비활성화되었습니다", null));
    }

    /**
     * 스케줄 삭제
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        log.info("DELETE /api/v1/schedules/{}", scheduleId);
        scheduleRuleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("스케줄이 삭제되었습니다", null));
    }
}