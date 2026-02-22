package com.example.chat.system.application.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.channel.metadata.ChannelMetadata;
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.system.application.dto.response.ChannelListItem;
import com.example.chat.system.application.query.ChannelListQuery;
import com.example.chat.system.application.query.ChannelSortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 Query Service
 *
 * CQRS Query Side:
 * - 복잡한 조회 로직
 * - 여러 Aggregate 조인
 * - 필터링/정렬/페이징
 */
@Service
@Transactional(readOnly = true)
public class ChannelQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelQueryService.class);

    private final ChannelRepository channelRepository;
    private final ChannelMetadataRepository metadataRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChannelQueryService(
            ChannelRepository channelRepository,
            ChannelMetadataRepository metadataRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.metadataRepository = metadataRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Page<ChannelListItem> getChannelList(ChannelListQuery query) {
        logger.debug("Getting channel list: userId={}, filters={}", query.userId(), query);

        UserId userId = UserId.of(query.userId());

        // 1. 사용자의 채널 목록 조회
        List<Channel> channels = channelRepository.findByMemberId(userId);
        if (channels.isEmpty()) {
            return Page.empty();
        }

        // 2. 채널 ID 리스트 추출
        List<ChannelId> channelIds = channels.stream().map(Channel::getId).collect(Collectors.toList());

        // 3. 메타데이터 배치 조회
        Map<ChannelId, ChannelMetadata> metadataMap = metadataRepository.findByChannelIdsAndUserId(channelIds, userId);

        // 4. 마지막 메시지 배치 조회
        Map<ChannelId, com.example.chat.domain.message.Message> lastMessageMap = messageRepository
                .findLastMessageByChannelIds(channelIds);

        // 5. ChannelListItem 변환
        List<ChannelListItem> items = channels.stream()
                .map(channel -> buildChannelListItem(
                        channel,
                        metadataMap.get(channel.getId()),
                        lastMessageMap.get(channel.getId()),
                        userId))
                .collect(Collectors.toList());

        // 6. 필터링
        List<ChannelListItem> filteredItems = applyFilters(items, query);

        // 7. 정렬
        List<ChannelListItem> sortedItems = applySorting(filteredItems, query);

        // 8. 페이징
        return applyPagination(sortedItems, query);
    }

    private ChannelListItem buildChannelListItem(
            Channel channel,
            ChannelMetadata metadata,
            com.example.chat.domain.message.Message lastMessage,
            UserId currentUserId) {
        // 메타데이터 정보
        int unreadCount = (metadata != null) ? metadata.getUnreadCount() : 0;
        boolean favorite = (metadata != null) && metadata.isFavorite();
        boolean pinned = (metadata != null) && metadata.isPinned();
        boolean notificationEnabled = (metadata == null) || metadata.isNotificationEnabled();
        Instant lastReadAt = (metadata != null) ? metadata.getLastReadAt() : null;
        Instant lastActivityAt = (metadata != null)
                ? (metadata.getLastActivityAt() != null ? metadata.getLastActivityAt() : metadata.getCreatedAt())
                : null;

        // 마지막 메시지 정보
        String lastMessageId = (lastMessage != null) ? lastMessage.getId().value() : null;
        String lastMessageContent = (lastMessage != null)
                ? (lastMessage.getContent() instanceof com.example.chat.domain.message.MessageContent.Text t ? t.text()
                        : null)
                : null;
        String lastMessageSenderId = (lastMessage != null) ? lastMessage.getSenderId().value() : null;
        Instant lastMessageTime = (lastMessage != null) ? lastMessage.getCreatedAt() : null;

        String lastMessageSenderName = (lastMessage != null)
                ? userRepository.findById(lastMessage.getSenderId()).map(User::getUsername).orElse(null)
                : null;

        // DIRECT 채널인 경우 상대방 정보
        String otherUserId = null;
        String otherUserName = null;
        String otherUserEmail = null;

        if (channel.getType() == ChannelType.DIRECT) {
            UserId otherId = getOtherUserId(channel, currentUserId);
            User user = userRepository.findById(otherId).orElse(null);
            if (user != null) {
                otherUserId = otherId.value();
                otherUserName = user.getUsername();
                otherUserEmail = user.getEmail();
            }
        }

        // GROUP 채널인 경우 소유자 정보
        String ownerUserId = null;
        String ownerUserName = null;

        if (channel.getType() == ChannelType.GROUP) {
            User owner = userRepository.findById(channel.getOwnerId()).orElse(null);
            if (owner != null) {
                ownerUserId = channel.getOwnerId().value();
                ownerUserName = owner.getUsername();
            }
        }

        return new ChannelListItem(
                channel.getId().value(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                channel.isActive(),
                lastMessageId,
                lastMessageContent,
                lastMessageSenderId,
                lastMessageSenderName,
                lastMessageTime,
                unreadCount,
                favorite,
                pinned,
                notificationEnabled,
                lastReadAt,
                lastActivityAt,
                channel.getMemberIds().size(),
                otherUserId,
                otherUserName,
                otherUserEmail,
                ownerUserId,
                ownerUserName,
                channel.getCreatedAt());
    }

    private UserId getOtherUserId(Channel channel, UserId myId) {
        return channel.getMemberIds().stream()
                .filter(id -> !id.equals(myId))
                .findFirst()
                .orElse(myId);
    }

    private List<ChannelListItem> applyFilters(List<ChannelListItem> items, ChannelListQuery query) {
        return items.stream()
                .filter(it -> query.type() == null || it.channelType() == query.type())
                .filter(it -> query.onlyFavorites() == null || !query.onlyFavorites() || it.favorite())
                .filter(it -> query.onlyUnread() == null || !query.onlyUnread() || it.unreadCount() > 0)
                .filter(it -> query.onlyPinned() == null || !query.onlyPinned() || it.pinned())
                .filter(it -> {
                    if (query.searchKeyword() == null || query.searchKeyword().isBlank())
                        return true;
                    String keyword = query.searchKeyword().toLowerCase();
                    return (it.channelName() != null && it.channelName().toLowerCase().contains(keyword)) ||
                            (it.otherUserName() != null && it.otherUserName().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());
    }

    private List<ChannelListItem> applySorting(List<ChannelListItem> items, ChannelListQuery query) {
        ChannelSortBy sortBy = query.sortBy();

        Comparator<ChannelListItem> comparator;
        switch (sortBy) {
            case NAME -> comparator = Comparator.comparing(it -> it.channelName() != null ? it.channelName() : "",
                    String.CASE_INSENSITIVE_ORDER);
            case UNREAD_COUNT -> comparator = Comparator.comparingInt(ChannelListItem::unreadCount);
            case CREATED_AT -> comparator = Comparator.comparing(ChannelListItem::createdAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case LAST_ACTIVITY -> {
                comparator = Comparator.comparing(ChannelListItem::pinned).reversed()
                        .thenComparator((a, b) -> {
                            Instant timeA = a.lastActivityAt() != null ? a.lastActivityAt()
                                    : (a.lastMessageTime() != null ? a.lastMessageTime() : a.createdAt());
                            Instant timeB = b.lastActivityAt() != null ? b.lastActivityAt()
                                    : (b.lastMessageTime() != null ? b.lastMessageTime() : b.createdAt());
                            return Objects.compare(timeB, timeA, Comparator.nullsLast(Comparator.naturalOrder()));
                        });
            }
            default -> comparator = (a, b) -> 0;
        }

        if (query.direction() == ChannelListQuery.SortDirection.ASC) {
            comparator = comparator.reversed();
        }

        return items.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Page<ChannelListItem> applyPagination(List<ChannelListItem> items, ChannelListQuery query) {
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), items.size());

        if (start >= items.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(query.page(), query.size()), items.size());
        }

        List<ChannelListItem> pageItems = items.subList(start, end);
        return new PageImpl<>(pageItems, PageRequest.of(query.page(), query.size()), items.size());
    }
}
