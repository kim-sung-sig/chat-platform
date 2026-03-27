package com.example.chat.scheduled.rest.dto.response;

import com.example.chat.message.domain.MessageContent;
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
        String contentType;
        String text = null;
        String mediaUrl = null;
        String fileName = null;

        switch (content) {
            case MessageContent.Text t -> {
                contentType = "TEXT";
                text = t.getText();
            }
            case MessageContent.Image img -> {
                contentType = "IMAGE";
                mediaUrl = img.getMediaUrl();
                fileName = img.getFileName();
            }
            case MessageContent.File f -> {
                contentType = "FILE";
                mediaUrl = f.getMediaUrl();
                fileName = f.getFileName();
            }
        }

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
                text,
                mediaUrl,
                fileName
        );
    }
}
