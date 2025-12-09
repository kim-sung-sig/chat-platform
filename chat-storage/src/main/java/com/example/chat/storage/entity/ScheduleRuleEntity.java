package com.example.chat.storage.entity;

import com.example.chat.domain.schedule.ScheduleStatus;
import com.example.chat.domain.schedule.ScheduleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schedule_rules", indexes = {
        @Index(name = "idx_schedule_type_status", columnList = "schedule_type, schedule_status"),
        @Index(name = "idx_schedule_scheduled_at", columnList = "scheduled_at")
})
public class ScheduleRuleEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false, length = 20)
    private ScheduleStatus scheduleStatus;

    // Message 정보 (임베디드)
    @Column(name = "message_id", nullable = false, length = 36)
    private String messageId;

    @Column(name = "channel_id", nullable = false, length = 36)
    private String channelId;

    @Column(name = "sender_id", nullable = false, length = 36)
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

    // 스케줄 정보
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;  // RECURRING일 때만 사용

    @Column(name = "scheduled_at")
    private Instant scheduledAt;  // ONE_TIME일 때만 사용

    // 타임스탬프
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.scheduleStatus == null) {
            this.scheduleStatus = ScheduleStatus.PENDING;
        }
    }
}
