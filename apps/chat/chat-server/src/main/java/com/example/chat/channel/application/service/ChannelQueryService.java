package com.example.chat.channel.application.service;
import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.storage.entity.*;
import com.example.chat.storage.repository.*;
import com.example.chat.channel.application.dto.response.ChannelListItem;
import com.example.chat.channel.application.query.ChannelListQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 채팅방 목록 조회 Query Service
 *
 * DB 레벨 페이징/필터 (메모리 필터 제거 완료):
 * - channelType 필터 -> JPA 쿼리
 * - favorite / pinned / unread / keyword -> LEFT JOIN metadata + JPQL WHERE 절
 * - page/size -> Spring Data Pageable
 *
 * N+1 방지: User / Member / Metadata / LastMessage 배치 조회
 */
@Service
@Transactional(readOnly = true)
public class ChannelQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelQueryService.class);
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;
    private final JpaChannelMetadataRepository metadataRepository;
    private final JpaMessageRepository messageRepository;
    private final JpaUserRepository userRepository;
    public ChannelQueryService(
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository,
            JpaChannelMetadataRepository metadataRepository,
            JpaMessageRepository messageRepository,
            JpaUserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.metadataRepository = metadataRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    public Page<ChannelListItem> getChannelList(ChannelListQuery query) {
        logger.debug("Getting channel list: userId={}, page={}, size={}", query.userId(), query.page(), query.size());
        String channelTypeStr = query.type() != null ? query.type().name() : null;
        boolean onlyFavorites = Boolean.TRUE.equals(query.onlyFavorites());
        boolean onlyPinned = Boolean.TRUE.equals(query.onlyPinned());
        boolean onlyUnread = Boolean.TRUE.equals(query.onlyUnread());
        String keyword = (query.searchKeyword() != null && !query.searchKeyword().isBlank())
                ? query.searchKeyword() : null;
        var pageable = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ChatChannelEntity> channelPage = channelRepository.findByMemberIdWithAllFilters(
                query.userId(), channelTypeStr,
                onlyFavorites, onlyPinned, onlyUnread, keyword,
                pageable);
        if (channelPage.isEmpty()) return Page.empty(pageable);
        List<ChatChannelEntity> channels = channelPage.getContent();
        List<String> channelIds = channels.stream().map(ChatChannelEntity::getId).collect(Collectors.toList());
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
        lastMessageMap.values().stream().map(ChatMessageEntity::getSenderId).forEach(requiredIds::add);
        return userRepository.findAllById(requiredIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
    }
    private ChannelListItem buildItem(ChatChannelEntity ch, Set<String> memberIds,
            ChatChannelMetadataEntity meta, ChatMessageEntity lastMsg,
            String currentUserId, Map<String, UserEntity> userCache) {
        MetaInfo metaInfo = extractMeta(meta);
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
    private MetaInfo extractMeta(ChatChannelMetadataEntity meta) {
        if (meta == null) return new MetaInfo(0, false, false, true, null, null);
        Instant activity = meta.getLastActivityAt() != null ? meta.getLastActivityAt() : meta.getCreatedAt();
        return new MetaInfo(meta.getUnreadCount(), meta.isFavorite(), meta.isPinned(),
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
            String otherId = memberIds.stream().filter(id -> !id.equals(currentUserId)).findFirst().orElse(null);
            if (otherId == null) return ParticipantInfo.empty();
            UserEntity other = userCache.get(otherId);
            return other == null ? ParticipantInfo.empty()
                    : new ParticipantInfo(otherId, other.getUsername(), other.getEmail(), null, null);
        }
        UserEntity owner = userCache.get(ch.getOwnerId());
        return owner == null ? ParticipantInfo.empty()
                : new ParticipantInfo(null, null, null, ch.getOwnerId(), owner.getUsername());
    }
    private record MetaInfo(int unreadCount, boolean favorite, boolean pinned,
                             boolean notificationEnabled, Instant lastReadAt, Instant lastActivityAt) {}
    private record LastMsgInfo(String id, String content, String senderId, String senderName, Instant time) {}
    private record ParticipantInfo(String otherUserId, String otherUserName, String otherUserEmail,
                                   String ownerUserId, String ownerUserName) {
        static ParticipantInfo empty() {
            return new ParticipantInfo(null, null, null, null, null);
        }
    }
}