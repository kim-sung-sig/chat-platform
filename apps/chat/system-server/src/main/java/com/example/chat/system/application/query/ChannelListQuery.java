package com.example.chat.system.application.query;

import com.example.chat.domain.channel.ChannelType;

/**
 * 채팅방 목록 조회 Query 모델
 *
 * CQRS Query Side:
 * - 복잡한 필터링/정렬 지원
 * - 페이징 지원
 */
public record ChannelListQuery(
        String userId,
        ChannelType type,
        Boolean onlyFavorites,
        Boolean onlyUnread,
        Boolean onlyPinned,
        String searchKeyword,
        ChannelSortBy sortBy,
        SortDirection direction,
        int page,
        int size) {
    public ChannelListQuery {
        if (sortBy == null)
            sortBy = ChannelSortBy.LAST_ACTIVITY;
        if (direction == null)
            direction = SortDirection.DESC;
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 20;
    }

    /**
     * 정렬 방향
     */
    public enum SortDirection {
        ASC, // 오름차순
        DESC // 내림차순
    }

    // Convenience constructor for minimal params
    public ChannelListQuery(String userId) {
        this(userId, null, null, null, null, null, ChannelSortBy.LAST_ACTIVITY, SortDirection.DESC, 0, 20);
    }
}
