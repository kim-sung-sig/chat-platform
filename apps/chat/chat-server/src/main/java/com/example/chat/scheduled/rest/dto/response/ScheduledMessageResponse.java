package com.example.chat.scheduled.rest.dto.response;

import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduleType;

import java.time.ZonedDateTime;

/**
 * 예약 메시지 응답 DTO
 */
public record ScheduledMessageResponse(
        String id,
        String channelId,
        String senderId,
        ScheduleType scheduleType,
        ScheduleStatus status,
        ZonedDateTime scheduledAt,
        ZonedDateTime createdAt,
        ZonedDateTime executedAt,
        ZonedDateTime cancelledAt,
        int retryCount,
        String contentType,
        String text,
        String mediaUrl,
        String fileName
) {
    public static ScheduledMessageResponse from(ScheduledMessage domain) {
        var content = domain.getContent();
        String contentType = switch (content) {
            case com.example.chat.message.domain.MessageContent.Text ignored -> "TEXT";
            case com.example.chat.message.domain.MessageContent.Image ignored -> "IMAGE";
            case com.example.chat.message.domain.MessageContent.File ignored -> "FILE";
        };

        return new ScheduledMessageResponse(
                domain.getId(),
                domain.getChannelId(),
                domain.getSenderId(),
                domain.getScheduleType(),
                domain.getStatus(),
                domain.getScheduledAt(),
                domain.getCreatedAt(),
                domain.getExecutedAt(),
                domain.getCancelledAt(),
                domain.getRetryCount(),
                contentType,
                content.getText(),
                content.getMediaUrl(),
                content.getFileName()
        );
    }
}
