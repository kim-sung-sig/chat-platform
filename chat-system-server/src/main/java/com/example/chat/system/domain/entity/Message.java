package com.example.chat.system.domain.entity;

import com.example.chat.system.domain.enums.MessageStatus;
import com.example.chat.system.domain.enums.MessageType;
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

/**
 * 메시지 엔티티
 * 채널 담당자가 작성한 발행 콘텐츠
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_channel_id", columnList = "channel_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_channel"))
    private Channel channel;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType; // TEXT, IMAGE, MIXED

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status; // DRAFT, SCHEDULED, PUBLISHED, CANCELLED

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 작성자 ID

    /**
     * 메시지 상태 변경
     */
    public void changeStatus(MessageStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 메시지 내용 수정
     */
    public void updateContent(String title, String content) {
        if (this.status != MessageStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT messages can be updated");
        }
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    /**
     * 메시지 발행 준비
     */
    public void prepareForPublish() {
        if (this.status != MessageStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT messages can be scheduled");
        }
        this.status = MessageStatus.SCHEDULED;
    }

    /**
     * 메시지 발행 완료
     */
    public void markAsPublished() {
        if (this.status != MessageStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED messages can be published");
        }
        this.status = MessageStatus.PUBLISHED;
    }

    /**
     * 메시지 취소
     */
    public void cancel() {
        if (this.status == MessageStatus.PUBLISHED) {
            throw new IllegalStateException("Published messages cannot be cancelled");
        }
        this.status = MessageStatus.CANCELLED;
    }
}