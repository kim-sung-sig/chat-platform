package com.example.chat.scheduled.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

/**
 * 메시지 예약 생성 요청 DTO
 *
 * contentType: TEXT | IMAGE | FILE
 * scheduledAt: now+5분 ~ now+30일 (서비스 레이어에서 검증)
 */
public record CreateScheduledMessageRequest(

        @NotBlank
        String channelId,

        @NotBlank
        String contentType,

        /** TEXT 타입인 경우 필수 */
        String text,

        /** IMAGE / FILE 타입인 경우 필수 */
        String mediaUrl,

        String fileName,

        Long fileSize,

        /** FILE 타입인 경우 필수 */
        String mimeType,

        @NotNull
        ZonedDateTime scheduledAt
) {
}
