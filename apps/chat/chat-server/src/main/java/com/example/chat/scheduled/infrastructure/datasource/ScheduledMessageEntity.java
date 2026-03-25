package com.example.chat.scheduled.infrastructure.datasource;

import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduleType;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import com.example.chat.message.domain.MessageContent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * schedule_rules 테이블 JPA Entity
 *
 * 도메인 객체(ScheduledMessage)와 분리된 영속성 모델.
 * toDomain() / fromDomain() 으로 변환한다.
 */
@Entity
@Table(name = "schedule_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduledMessageEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", length = 20, nullable = false)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", length = 20, nullable = false)
    private ScheduleStatus scheduleStatus;

    /** 예약 생성 시점에는 null; 발송 완료 후 채워진다 (V10 migration: NOT NULL → nullable) */
    @Column(name = "message_id", length = 36)
    private String messageId;

    @Column(name = "channel_id", length = 36, nullable = false)
    private String channelId;

    @Column(name = "sender_id", length = 36, nullable = false)
    private String senderId;

    @Column(name = "message_text", length = 5000)
    private String messageText;

    @Column(name = "message_media_url", length = 500)
    private String messageMediaUrl;

    @Column(name = "message_file_name", length = 255)
    private String messageFileName;

    @Column(name = "message_file_size")
    private Long messageFileSize;

    @Column(name = "message_mime_type", length = 100)
    private String messageMimeType;

    @Column(name = "scheduled_at")
    private java.time.LocalDateTime scheduledAt;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "executed_at")
    private java.time.LocalDateTime executedAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    // ── 변환 ─────────────────────────────────────────────────────────

    public ScheduledMessage toDomain() {
        MessageContent content = resolveContent();
        return new ScheduledMessage(
                id,
                channelId,
                senderId,
                content,
                scheduleType,
                scheduleStatus,
                toZoned(scheduledAt),
                toZoned(createdAt),
                toZoned(executedAt),
                toZoned(cancelledAt),
                retryCount
        );
    }

    public static ScheduledMessageEntity fromDomain(ScheduledMessage domain) {
        ScheduledMessageEntity e = new ScheduledMessageEntity();
        e.id = domain.getId();
        e.scheduleType = domain.getScheduleType();
        e.scheduleStatus = domain.getStatus();
        e.channelId = domain.getChannelId();
        e.senderId = domain.getSenderId();
        e.retryCount = domain.getRetryCount();
        e.scheduledAt = toLocal(domain.getScheduledAt());
        e.createdAt = toLocal(domain.getCreatedAt());
        e.executedAt = toLocal(domain.getExecutedAt());
        e.cancelledAt = toLocal(domain.getCancelledAt());

        MessageContent content = domain.getContent();
        if (content instanceof MessageContent.Text t) {
            e.messageText = t.getText();
        } else if (content instanceof MessageContent.Image img) {
            e.messageMediaUrl = img.getMediaUrl();
            e.messageFileName = img.getFileName();
            e.messageFileSize = img.getFileSize();
        } else if (content instanceof MessageContent.File f) {
            e.messageMediaUrl = f.getMediaUrl();
            e.messageFileName = f.getFileName();
            e.messageFileSize = f.getFileSize();
            e.messageMimeType = f.getMimeType();
        }

        return e;
    }

    // ── private helpers ───────────────────────────────────────────────

    private MessageContent resolveContent() {
        if (messageText != null) {
            return MessageContent.text(messageText);
        }
        if (messageMimeType != null) {
            return MessageContent.file(messageMediaUrl, messageFileName, messageFileSize, messageMimeType);
        }
        if (messageMediaUrl != null) {
            return MessageContent.image(messageMediaUrl, messageFileName, messageFileSize);
        }
        throw new IllegalStateException("ScheduledMessageEntity has no content: id=" + id);
    }

    private static ZonedDateTime toZoned(java.time.LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneId.of("UTC"));
    }

    private static java.time.LocalDateTime toLocal(ZonedDateTime zdt) {
        return zdt == null ? null : zdt.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }
}
