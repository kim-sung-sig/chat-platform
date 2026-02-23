package com.example.chat.push.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 푸시 메시지 엔티티
 */
@Entity
@Table(name = "push_messages")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PushMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String targetUserId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private PushStatus status = PushStatus.PENDING;

    @Column(nullable = false)
    private String pushType;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    @Setter
    private LocalDateTime processedAt;

    @Column
    @Setter
    private String errorMessage;

    @Version
    @Setter
    private Long version = 0L;

    private PushMessage(String targetUserId, String title, String content, String pushType) {
        this.targetUserId = targetUserId;
        this.title = title;
        this.content = content;
        this.pushType = pushType;
    }

    public static PushMessage of(String targetUserId, String title, String content, String pushType) {
        return new PushMessage(targetUserId, title, content, pushType);
    }

    public void markProcessing() {
        this.status = PushStatus.PROCESSING;
    }

    public void markCompleted() {
        this.status = PushStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = PushStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
