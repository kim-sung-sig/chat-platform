package com.example.chat.storage.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채널 내 사용자의 읽기 진행 마커 값 객체.
 * chat_channel_metadata 테이블의 last_read_* 컬럼 그룹을 캡슐화한다.
 * 컬럼 이름은 기존 DDL 을 그대로 유지한다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LastReadPointer {

    @Column(name = "last_read_message_id", length = 36)
    private String lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    private LastReadPointer(String lastReadMessageId, Instant lastReadAt) {
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = lastReadAt;
    }

    /**
     * 아직 읽은 기록이 없는 빈 포인터를 생성한다.
     */
    public static LastReadPointer empty() {
        return new LastReadPointer(null, null);
    }

    /**
     * 특정 메시지를 읽은 시점의 포인터를 생성한다.
     */
    public static LastReadPointer of(String messageId, Instant readAt) {
        return new LastReadPointer(messageId, readAt);
    }

    /**
     * 주어진 메시지로 읽기 마커를 갱신한 새 인스턴스를 반환한다.
     */
    public LastReadPointer advance(String messageId) {
        return new LastReadPointer(messageId, Instant.now());
    }
}
