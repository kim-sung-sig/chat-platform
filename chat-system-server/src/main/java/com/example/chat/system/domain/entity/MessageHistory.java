package com.example.chat.system.domain.entity;

import com.example.chat.system.domain.enums.PublishStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 발행 이력 엔티티
 * 실제 발행된 메시지 및 발행 상태 (성공, 실패, 재시도 등)
 */
@Entity
@Table(name = "message_histories", indexes = {
    @Index(name = "idx_message_id", columnList = "message_id"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_publish_status", columnList = "publish_status"),
    @Index(name = "idx_published_at", columnList = "published_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_message"))
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 20)
    private PublishStatus publishStatus; // PENDING, SUCCESS, FAILED, RETRY

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "schedule_rule_id")
    private Long scheduleRuleId; // 어떤 스케줄 규칙으로 발행되었는지

    /**
     * 발행 성공 처리
     */
    public void markAsSuccess() {
        this.publishStatus = PublishStatus.SUCCESS;
        this.publishedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * 발행 실패 처리
     */
    public void markAsFailed(String errorMessage) {
        this.publishStatus = PublishStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 재시도 처리
     */
    public void markAsRetry(String errorMessage) {
        this.publishStatus = PublishStatus.RETRY;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry(int maxRetryCount) {
        return this.retryCount < maxRetryCount;
    }
}