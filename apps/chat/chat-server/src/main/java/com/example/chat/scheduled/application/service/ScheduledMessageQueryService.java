package com.example.chat.scheduled.application.service;

import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;

import java.util.List;

/**
 * 예약 발송 Query 서비스 Port
 *
 * 읽기 작업 전담 (CQRS) — replica 데이터소스 사용
 */
public interface ScheduledMessageQueryService {

    /**
     * 채널의 예약 메시지 목록 조회 (발신자 기준, 최신순)
     *
     * @param channelId 채널 ID
     * @param senderId  발신자 ID
     * @return 예약 메시지 응답 목록
     */
    List<ScheduledMessageResponse> listScheduledMessages(String channelId, String senderId);
}
