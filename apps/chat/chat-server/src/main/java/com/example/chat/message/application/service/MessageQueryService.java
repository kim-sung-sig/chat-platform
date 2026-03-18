package com.example.chat.message.application.service;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.message.rest.dto.response.CursorPageResponse;
import com.example.chat.message.rest.dto.response.MessageResponse;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.storage.domain.repository.JpaMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 조회 Query Service
 *
 * 책임: cursor 기반 페이징 메시지 조회
 * - GET /api/messages/{channelId}?cursor=...&limit=...
 */
@Service
@Transactional(readOnly = true)
public class MessageQueryService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueryService.class);
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final JpaMessageRepository messageRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;

    public MessageQueryService(
            JpaMessageRepository messageRepository,
            JpaChannelRepository channelRepository,
            JpaChannelMemberRepository channelMemberRepository) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
    }

    /**
     * 채널 메시지 목록 조회 (cursor 기반 페이징)
     *
     * @param channelId 채널 ID
     * @param cursor    이전 페이지 마지막 메시지의 createdAt (null 이면 최신부터)
     * @param limit     페이지 크기 (max 100)
     * @return cursor 페이지 응답
     */
    public CursorPageResponse<MessageResponse> getMessages(String channelId, String cursor, int limit) {
        log.debug("Getting messages: channelId={}, cursor={}, limit={}", channelId, cursor, limit);

        String currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        channelRepository.findById(channelId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHANNEL_NOT_FOUND));

        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, currentUserId)) {
            throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
        }

        int safeLimit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
        // limit+1 개 조회 → hasNext 판단
        var pageable = PageRequest.of(0, safeLimit + 1);

        var entities = (cursor == null || cursor.isBlank())
                ? messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable)
                : messageRepository.findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId, Instant.parse(cursor), pageable);

        boolean hasNext = entities.size() > safeLimit;
        var page = hasNext ? entities.subList(0, safeLimit) : entities;

        List<MessageResponse> items = page.stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());

        String nextCursor = hasNext
                ? page.get(page.size() - 1).getCreatedAt().toString()
                : null;

        return new CursorPageResponse<>(items, nextCursor, hasNext, items.size());
    }
}
