package com.example.chat.system.domain.entity;

import com.example.chat.system.domain.enums.ScheduleType;
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
 * 스케줄 규칙 엔티티
 * 주기적/단발성 메시지 발행 규칙 (Cron, Delay 등)
 */
@Entity
@Table(name = "schedule_rules", indexes = {
    @Index(name = "idx_message_id", columnList = "message_id"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_next_execution_time", columnList = "next_execution_time")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_message"))
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleType scheduleType; // ONCE, RECURRING

    @Column(name = "cron_expression", length = 100)
    private String cronExpression; // Quartz Cron 표현식 (주기 발행용)

    @Column(name = "execution_time")
    private LocalDateTime executionTime; // 단발 발행 시간

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime; // 다음 실행 시간

    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime; // 마지막 실행 시간

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "job_name", unique = true, length = 200)
    private String jobName; // Quartz Job 식별자

    @Column(name = "job_group", length = 200)
    private String jobGroup; // Quartz Job 그룹

    @Column(name = "execution_count", nullable = false)
    private Integer executionCount; // 실행 횟수

    @Column(name = "max_execution_count")
    private Integer maxExecutionCount; // 최대 실행 횟수 (null이면 무제한)

    /**
     * 스케줄 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 스케줄 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 다음 실행 시간 업데이트
     */
    public void updateNextExecutionTime(LocalDateTime nextTime) {
        this.nextExecutionTime = nextTime;
    }

    /**
     * 실행 완료 처리
     */
    public void markAsExecuted() {
        this.lastExecutionTime = LocalDateTime.now();
        this.executionCount++;

        // 최대 실행 횟수에 도달하면 비활성화
        if (maxExecutionCount != null && executionCount >= maxExecutionCount) {
            this.isActive = false;
        }
    }

    /**
     * 실행 가능 여부 확인
     */
    public boolean canExecute() {
        if (!isActive) {
            return false;
        }

        if (maxExecutionCount != null && executionCount >= maxExecutionCount) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (scheduleType == ScheduleType.ONCE) {
            return executionTime != null && now.isAfter(executionTime) && executionCount == 0;
        }

        return nextExecutionTime != null && now.isAfter(nextExecutionTime);
    }
}