package com.example.chat.channel.application.query;

import java.time.Instant;

import com.example.chat.common.core.enums.ChannelType;

/**
 * 채팅방 목록 조회 Query 모델 (Cursor-based pagination)
 *
 * CQRS Query Side:
 * - 커서 기반 페이징 — offset 사용 금지
 *   cursor: 마지막으로 수신한 채널의 createdAt (null 이면 첫 페이지)
 * - 정렬은 createdAt DESC 고정
 */
public record ChannelListQuery(
        String userId,
        ChannelType type,
        Boolean onlyFavorites,
        Boolean onlyUnread,
        Boolean onlyPinned,
        String searchKeyword,
        Instant cursor,
        int size) {
    public ChannelListQuery {
        if (size <= 0)
            size = 20;
    }

    // Convenience constructor for minimal params
    public ChannelListQuery(String userId) {
        this(userId, null, null, null, null, null, null, 20);
    }
}
