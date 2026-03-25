package com.example.chat.scheduled.application.service;

import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.rest.dto.request.CreateScheduledMessageRequest;
import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;

/**
 * 예약 발송 Command 서비스 Port
 *
 * 쓰기 작업 전담 (CQRS)
 */
public interface ScheduledMessageCommandService {

    /**
     * 메시지 예약 생성
     *
     * @param senderId 발신자 ID (SecurityUtils에서 추출)
     * @param request  예약 생성 요청
     * @return 생성된 예약 메시지 응답
     */
    ScheduledMessageResponse createScheduledMessage(String senderId, CreateScheduledMessageRequest request);

    /**
     * 예약 취소 (PENDING 상태에서만 가능)
     *
     * @param scheduledMessageId 예약 ID
     * @param requesterId        취소 요청자 ID
     */
    void cancelScheduledMessage(String scheduledMessageId, String requesterId);

    /**
     * Quartz Job에서 호출: PENDING → EXECUTING 전이 + 메시지 발송
     *
     * @param scheduledMessageId 예약 ID
     */
    void executeScheduledMessage(String scheduledMessageId);
}
