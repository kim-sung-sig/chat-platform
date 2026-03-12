package com.example.chat.channel.application.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.cache.UnreadCacheService;
import com.example.chat.channel.application.dto.response.ChannelListItem;
import com.example.chat.channel.application.query.ChannelListQuery;
import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatChannelMemberEntity;
import com.example.chat.storage.entity.ChatChannelMetadataEntity;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.entity.UserEntity;
import com.example.chat.storage.repository.JpaChannelMemberRepository;
import com.example.chat.storage.repository.JpaChannelMetadataRepository;
import com.example.chat.storage.repository.JpaChannelRepository;
import com.example.chat.storage.repository.JpaMessageRepository;
import com.example.chat.storage.repository.JpaUserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 목록 조회 전용 Read Model Service (Phase 11 — CQRS)
 *
 * CQRS 분리 원칙:
 *   Write Model: ChannelCommandService, MessageSendService, ChannelMetadataApplicationService
 *                → PostgreSQL Source(Write DB) 직접 기록
 *   Read Model (이 클래스): ChannelListQueryService
 *                → PostgreSQL Replica (readOnly=true 트랜잭션 → ReplicaDataSource 라우팅)
 *                → Redis Hash 캐시 우선 조회 (UnreadCacheService)
 *
 * unreadCount 조회 전략 (Two-Level Read):
 *   L1: Redis HGET chat:channel:{channelId}:unread {userId}
 *       → hit:  캐시 값 사용 (< 1ms)
 *       → miss: PostgreSQL metadata.unreadCount 사용 (fallback)
 *   이를 통해 채널 목록 조회 시 PostgreSQL unreadCount 집계 쿼리 부하 제거
 *
 * N+1 방지: User / Member / Metadata / LastMessage 배치 조회 유지
 */
@Service
@Transactional(readOnly = true)   // → TransactionRoutingDataSource: REPLICA 자동 라우팅
@Slf4j
public class ChannelListQueryService {

    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final JpaChannelMetadataRepository metadataRepository;
    private final JpaMessageRepository messageRepository;
    private final JpaUserRepository userRepository;
    private final UnreadCacheService unreadCacheService;

    public ChannelListQueryService(
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository,
            JpaChannelMetadataRepository metadataRepository,
            JpaMessageRepository messageRepository,
            JpaUserRepository userRepository,
            UnreadCacheService unreadCacheService) {
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.metadataRepository = metadataRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.unreadCacheService = unreadCacheService;
    }

    /**
     * 채팅방 목록 조회 (Read Model)
     *
     * 단계:
     * 1. PostgreSQL Replica에서 필터/페이징 적용한 채널 목록 조회
     * 2. 멤버/메타데이터/마지막메시지 배치 fetch (N+1 방지)
     * 3. unreadCount: Redis L1 → miss 시 PostgreSQL L2 fallback
     */
    public Page<ChannelListItem> getChannelList(ChannelListQuery query) {
        log.debug("ChannelListQueryService: userId={}, page={}/{}", query.userId(), query.page(), query.size());

        String channelTypeStr = query.type() != null ? query.type().name() : null;
        boolean onlyFavorites  = Boolean.TRUE.equals(query.onlyFavorites());
        boolean onlyPinned     = Boolean.TRUE.equals(query.onlyPinned());
        boolean onlyUnread     = Boolean.TRUE.equals(query.onlyUnread());
        String keyword = (query.searchKeyword() != null && !query.searchKeyword().isBlank())
                ? query.searchKeyword() : null;

        var pageable = PageRequest.of(query.page(), query.size(),
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<ChatChannelEntity> channelPage = channelRepository.findByMemberIdWithAllFilters(
                query.userId(), channelTypeStr,
                onlyFavorites, onlyPinned, onlyUnread, keyword,
                pageable);

        if (channelPage.isEmpty()) return Page.empty(pageable);

        List<ChatChannelEntity> channels = channelPage.getContent();
        List<String> channelIds = channels.stream()
                .map(ChatChannelEntity::getId)
                .collect(Collectors.toList());

        // 배치 조회 (N+1 방지)
        Map<String, Set<String>> membersByChannel = fetchMemberIdsBatch(channelIds);
        Map<String, ChatChannelMetadataEntity> metadataMap = fetchMetadataMap(channelIds, query.userId());
        Map<String, ChatMessageEntity> lastMessageMap = fetchLastMessageMap(channelIds);
        Map<String, UserEntity> userCache = buildUserCache(channels, lastMessageMap, query.userId(), membersByChannel);

        List<ChannelListItem> items = channels.stream()
                .map(ch -> buildItem(ch,
                        membersByChannel.getOrDefault(ch.getId(), Set.of()),
                        metadataMap.get(ch.getId()),
                        lastMessageMap.get(ch.getId()),
                        query.userId(), userCache))
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, channelPage.getTotalElements());
    }

    // ─────────────────────────────────────────────
    // Batch Fetch Helpers (N+1 방지)
    // ─────────────────────────────────────────────

    private Map<String, Set<String>> fetchMemberIdsBatch(List<String> channelIds) {
        return channelMemberRepository.findByChannelIdIn(channelIds).stream()
                .collect(Collectors.groupingBy(
                        ChatChannelMemberEntity::getChannelId,
                        Collectors.mapping(ChatChannelMemberEntity::getUserId, Collectors.toSet())));
    }

    private Map<String, ChatChannelMetadataEntity> fetchMetadataMap(List<String> channelIds, String userId) {
        return metadataRepository.findByChannelIdsAndUserId(channelIds, userId).stream()
                .collect(Collectors.toMap(ChatChannelMetadataEntity::getChannelId, m -> m));
    }

    private Map<String, ChatMessageEntity> fetchLastMessageMap(List<String> channelIds) {
        return messageRepository.findLastMessagesByChannelIds(channelIds).stream()
                .collect(Collectors.toMap(ChatMessageEntity::getChannelId, m -> m,
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b));
    }

    private Map<String, UserEntity> buildUserCache(
            List<ChatChannelEntity> channels,
            Map<String, ChatMessageEntity> lastMessageMap,
            String currentUserId,
            Map<String, Set<String>> membersByChannel) {

        Set<String> requiredIds = new HashSet<>();
        for (ChatChannelEntity ch : channels) {
            if (ch.getChannelType() == ChannelType.DIRECT) {
                membersByChannel.getOrDefault(ch.getId(), Set.of()).stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst().ifPresent(requiredIds::add);
            } else {
                requiredIds.add(ch.getOwnerId());
            }
        }
        lastMessageMap.values().stream()
                .map(ChatMessageEntity::getSenderId)
                .forEach(requiredIds::add);

        return userRepository.findAllById(requiredIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
    }

    // ─────────────────────────────────────────────
    // Item Builder
    // ─────────────────────────────────────────────

    private ChannelListItem buildItem(ChatChannelEntity ch, Set<String> memberIds,
            ChatChannelMetadataEntity meta, ChatMessageEntity lastMsg,
            String currentUserId, Map<String, UserEntity> userCache) {

        MetaInfo metaInfo = extractMeta(meta, ch.getId(), currentUserId);
        LastMsgInfo msgInfo = extractLastMsg(lastMsg, userCache);
        ParticipantInfo participant = extractParticipant(ch, memberIds, currentUserId, userCache);

        return new ChannelListItem(
                ch.getId(), ch.getName(), ch.getDescription(), ch.getChannelType(), ch.isActive(),
                msgInfo.id(), msgInfo.content(), msgInfo.senderId(), msgInfo.senderName(), msgInfo.time(),
                metaInfo.unreadCount(), metaInfo.favorite(), metaInfo.pinned(),
                metaInfo.notificationEnabled(), metaInfo.lastReadAt(), metaInfo.lastActivityAt(),
                memberIds.size(),
                participant.otherUserId(), participant.otherUserName(), participant.otherUserEmail(),
                participant.ownerUserId(), participant.ownerUserName(),
                ch.getCreatedAt());
    }

    /**
     * 미읽음 수 조회 — Two-Level Read
     *
     * L1 Redis:
     *   hit  → Redis 값 사용 (< 1ms, DB 부하 없음)
     *   miss → L2 PostgreSQL metadata.unreadCount fallback
     *
     * Redis 장애 시 자동 fallback (UnreadCacheService 내부 예외 처리)
     */
    private MetaInfo extractMeta(ChatChannelMetadataEntity meta, String channelId, String userId) {
        // L1: Redis 캐시 조회
        int unreadCount = unreadCacheService.getUnreadCount(channelId, userId)
                // L2: PostgreSQL fallback
                .orElseGet(() -> meta != null ? meta.getUnreadCount() : 0);

        if (meta == null) return new MetaInfo(unreadCount, false, false, true, null, null);

        Instant activity = meta.getLastActivityAt() != null
                ? meta.getLastActivityAt() : meta.getCreatedAt();

        return new MetaInfo(unreadCount, meta.isFavorite(), meta.isPinned(),
                meta.isNotificationEnabled(), meta.getLastReadAt(), activity);
    }

    private LastMsgInfo extractLastMsg(ChatMessageEntity msg, Map<String, UserEntity> userCache) {
        if (msg == null) return new LastMsgInfo(null, null, null, null, null);
        String content = switch (msg.getMessageType()) {
            case TEXT, SYSTEM       -> msg.getContentText() != null ? msg.getContentText() : "";
            case IMAGE              -> "[Image] " + msg.getContentFileName();
            case FILE, VIDEO, AUDIO -> "[File] " + msg.getContentFileName();
        };
        String senderName = Optional.ofNullable(userCache.get(msg.getSenderId()))
                .map(UserEntity::getUsername).orElse(null);
        return new LastMsgInfo(msg.getId(), content, msg.getSenderId(), senderName, msg.getCreatedAt());
    }

    private ParticipantInfo extractParticipant(ChatChannelEntity ch, Set<String> memberIds,
            String currentUserId, Map<String, UserEntity> userCache) {
        if (ch.getChannelType() == ChannelType.DIRECT) {
            String otherId = memberIds.stream()
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst().orElse(null);
            if (otherId == null) return ParticipantInfo.empty();
            UserEntity other = userCache.get(otherId);
            return other == null ? ParticipantInfo.empty()
                    : new ParticipantInfo(otherId, other.getUsername(), other.getEmail(), null, null);
        }
        UserEntity owner = userCache.get(ch.getOwnerId());
        return owner == null ? ParticipantInfo.empty()
                : new ParticipantInfo(null, null, null, ch.getOwnerId(), owner.getUsername());
    }

    // ─────────────────────────────────────────────
    // Private Records
    // ─────────────────────────────────────────────

    private record MetaInfo(int unreadCount, boolean favorite, boolean pinned,
                             boolean notificationEnabled, Instant lastReadAt, Instant lastActivityAt) {}

    private record LastMsgInfo(String id, String content, String senderId,
                                String senderName, Instant time) {}

    private record ParticipantInfo(String otherUserId, String otherUserName, String otherUserEmail,
                                   String ownerUserId, String ownerUserName) {
        static ParticipantInfo empty() {
            return new ParticipantInfo(null, null, null, null, null);
        }
    }
}
