package com.example.chat.system.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.ChannelRepository;
import com.example.chat.domain.channel.metadata.ChannelMetadata;
import com.example.chat.domain.channel.metadata.ChannelMetadataRepository;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.service.DomainException;
import com.example.chat.domain.user.UserId;
import com.example.chat.system.application.dto.response.ChannelMetadataResponse;
import com.example.chat.system.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 메타데이터 Application Service
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChannelMetadataApplicationService {

    private final ChannelMetadataRepository metadataRepository;
    private final ChannelRepository channelRepository;

    public ChannelMetadataResponse getOrCreateMetadata(String userId, String channelId) {
        log.debug("Getting or creating metadata: userId={}, channelId={}", userId, channelId);

        ChannelId cid = ChannelId.of(channelId);
        UserId uid = UserId.of(userId);

        // 채널 존재 및 멤버 확인
        Channel channel = channelRepository.findById(cid)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + channelId));

        if (!channel.isMember(uid)) {
            throw new DomainException("User is not a member of the channel");
        }

        // 메타데이터 조회 또는 생성
        ChannelMetadata metadata = metadataRepository.findByChannelIdAndUserId(cid, uid)
                .orElseGet(() -> {
                    ChannelMetadata newMetadata = ChannelMetadata.create(cid, uid);
                    return metadataRepository.save(newMetadata);
                });

        return ChannelMetadataResponse.from(metadata);
    }

    public ChannelMetadataResponse markAsRead(String userId, String channelId, String messageId) {
        log.info("Marking as read: userId={}, channelId={}, messageId={}", userId, channelId, messageId);

        ChannelMetadata metadata = findMetadata(userId, channelId);
        metadata.markAsRead(MessageId.of(messageId));

        return ChannelMetadataResponse.from(metadataRepository.save(metadata));
    }

    public void incrementUnreadCount(String userId, String channelId) {
        log.debug("Incrementing unread count: userId={}, channelId={}", userId, channelId);

        ChannelId cid = ChannelId.of(channelId);
        UserId uid = UserId.of(userId);

        ChannelMetadata metadata = metadataRepository.findByChannelIdAndUserId(cid, uid)
                .orElseGet(() -> ChannelMetadata.create(cid, uid));

        metadata.incrementUnreadCount();
        metadataRepository.save(metadata);
    }

    public ChannelMetadataResponse toggleNotification(String userId, String channelId) {
        log.info("Toggling notification: userId={}, channelId={}", userId, channelId);

        ChannelMetadata metadata = findMetadata(userId, channelId);
        metadata.toggleNotification();

        return ChannelMetadataResponse.from(metadataRepository.save(metadata));
    }

    public ChannelMetadataResponse toggleFavorite(String userId, String channelId) {
        log.info("Toggling favorite: userId={}, channelId={}", userId, channelId);

        ChannelMetadata metadata = findMetadata(userId, channelId);
        metadata.toggleFavorite();

        return ChannelMetadataResponse.from(metadataRepository.save(metadata));
    }

    public ChannelMetadataResponse togglePinned(String userId, String channelId) {
        log.info("Toggling pinned: userId={}, channelId={}", userId, channelId);

        ChannelMetadata metadata = findMetadata(userId, channelId);
        metadata.togglePinned();

        return ChannelMetadataResponse.from(metadataRepository.save(metadata));
    }

    @Transactional(readOnly = true)
    public List<ChannelMetadataResponse> getFavorites(String userId) {
        log.debug("Getting favorites for user: {}", userId);

        return metadataRepository.findFavoritesByUserId(UserId.of(userId)).stream()
                .map(ChannelMetadataResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChannelMetadataResponse> getPinned(String userId) {
        log.debug("Getting pinned channels for user: {}", userId);

        return metadataRepository.findPinnedByUserId(UserId.of(userId)).stream()
                .map(ChannelMetadataResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChannelMetadataResponse> getWithUnread(String userId) {
        log.debug("Getting channels with unread messages for user: {}", userId);

        return metadataRepository.findWithUnreadByUserId(UserId.of(userId)).stream()
                .map(ChannelMetadataResponse::from)
                .collect(Collectors.toList());
    }

    private ChannelMetadata findMetadata(String userId, String channelId) {
        return metadataRepository.findByChannelIdAndUserId(
                ChannelId.of(channelId),
                UserId.of(userId)).orElseThrow(() -> new ResourceNotFoundException("Channel metadata not found"));
    }
}
