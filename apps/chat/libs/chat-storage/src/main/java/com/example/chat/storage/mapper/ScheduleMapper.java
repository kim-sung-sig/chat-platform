package com.example.chat.storage.mapper;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.message.MessageType;
import com.example.chat.domain.schedule.CronExpression;
import com.example.chat.domain.schedule.ScheduleId;
import com.example.chat.domain.schedule.ScheduleRule;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ScheduleRuleEntity;
import org.springframework.stereotype.Component;

/**
 * ScheduleRule Domain ↔ ScheduleRuleEntity 변환
 */
@Component
public class ScheduleMapper {

    /**
     * Domain → Entity 변환
     */
    public ScheduleRuleEntity toEntity(ScheduleRule scheduleRule) {
        Message message = scheduleRule.getMessage();
        MessageContent content = message.getContent();

        return ScheduleRuleEntity.builder()
                .id(scheduleRule.getId().getValue())
                .scheduleType(scheduleRule.getType())
                .scheduleStatus(scheduleRule.getStatus())
                // Message 정보
                .messageId(message.getId().getValue())
                .channelId(message.getChannelId().getValue())
                .senderId(message.getSenderId().getValue())
                .messageText(content.getText())
                .messageMediaUrl(content.getMediaUrl())
                .messageFileName(content.getFileName())
                .messageFileSize(content.getFileSize())
                .messageMimeType(content.getMimeType())
                // 스케줄 정보
                .cronExpression(scheduleRule.getCronExpression() != null ?
                        scheduleRule.getCronExpression().getValue() : null)
                .scheduledAt(scheduleRule.getScheduledAt())
                // 타임스탬프
                .createdAt(scheduleRule.getCreatedAt())
                .executedAt(scheduleRule.getExecutedAt())
                .cancelledAt(scheduleRule.getCancelledAt())
                .build();
    }

    /**
     * Entity → Domain 변환
     */
    public ScheduleRule toDomain(ScheduleRuleEntity entity) {
        // MessageContent 복원
        MessageContent content = MessageContent.builder()
                .text(entity.getMessageText())
                .mediaUrl(entity.getMessageMediaUrl())
                .fileName(entity.getMessageFileName())
                .fileSize(entity.getMessageFileSize())
                .mimeType(entity.getMessageMimeType())
                .build();

        // MessageType 추론 (content 기반)
        MessageType messageType = inferMessageType(content);

        // Message 복원
        Message message = Message.builder()
                .id(MessageId.of(entity.getMessageId()))
                .channelId(ChannelId.of(entity.getChannelId()))
                .senderId(UserId.of(entity.getSenderId()))
                .content(content)
                .type(messageType)
                .createdAt(entity.getCreatedAt())
                .build();

        // CronExpression 복원 (RECURRING인 경우만)
        CronExpression cronExpression = entity.getCronExpression() != null ?
                CronExpression.of(entity.getCronExpression()) : null;

        return ScheduleRule.builder()
                .id(ScheduleId.of(entity.getId()))
                .type(entity.getScheduleType())
                .status(entity.getScheduleStatus())
                .message(message)
                .cronExpression(cronExpression)
                .scheduledAt(entity.getScheduledAt())
                .createdAt(entity.getCreatedAt())
                .executedAt(entity.getExecutedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }

    /**
     * MessageContent로부터 MessageType 추론
     */
    private MessageType inferMessageType(MessageContent content) {
        if (content.getMediaUrl() != null) {
            if (content.getMimeType() != null && content.getMimeType().startsWith("image/")) {
                return MessageType.IMAGE;
            }
            return MessageType.FILE;
        }
        return MessageType.TEXT;
    }
}
