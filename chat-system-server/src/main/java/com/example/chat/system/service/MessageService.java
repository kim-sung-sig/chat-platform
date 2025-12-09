package com.example.chat.system.service;

import com.example.chat.system.domain.entity.Channel;
import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.enums.MessageStatus;
import com.example.chat.system.dto.request.MessageCreateRequest;
import com.example.chat.system.dto.request.MessageUpdateRequest;
import com.example.chat.system.dto.response.MessageResponse;
import com.example.chat.system.exception.BusinessException;
import com.example.chat.system.exception.ResourceNotFoundException;
import com.example.chat.system.repository.ChannelRepository;
import com.example.chat.system.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 관리 서비스
 * 책임: 메시지 CRUD, 상태 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;

    /**
     * 메시지 생성 (Key 기반 도메인 조회 후 조립, 조기 리턴)
     */
    @Transactional
    public MessageResponse createMessage(MessageCreateRequest request) {
        log.info("Creating message for channel: {}", request.getChannelId());

        // Step 1: Key 기반 도메인 조회
        Channel channel = findChannelById(request.getChannelId());

        // Step 2: Early return - 채널 활성화 검증
        if (!channel.getIsActive()) {
            log.warn("Inactive channel attempted: channelId={}", request.getChannelId());
            throw new BusinessException("비활성화된 채널에는 메시지를 생성할 수 없습니다");
        }

        // Step 3: 도메인 조립
        Message message = assembleMessage(channel, request);

        // Step 4: 저장
        Message savedMessage = messageRepository.save(message);
        log.info("Message created successfully: messageId={}", savedMessage.getId());

        return MessageResponse.from(savedMessage);
    }

    /**
     * 채널 조회 (Key 기반)
     */
    private Channel findChannelById(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.error("Channel not found: channelId={}", channelId);
                    return new ResourceNotFoundException("Channel", channelId);
                });
    }

    /**
     * 메시지 도메인 조립
     */
    private Message assembleMessage(Channel channel, MessageCreateRequest request) {
        return Message.builder()
                .channel(channel)
                .title(request.getTitle())
                .content(request.getContent())
                .messageType(request.getMessageType())
                .status(MessageStatus.DRAFT)
                .createdBy(request.getCreatedBy())
                .build();
    }

    /**
     * 메시지 조회 (Key 기반)
     */
    public MessageResponse getMessage(Long messageId) {
        Message message = findMessageById(messageId);
        return MessageResponse.from(message);
    }

    /**
     * 채널별 메시지 목록 조회
     */
    public Page<MessageResponse> getMessagesByChannel(Long channelId, Pageable pageable) {
        // Early return: channelId 검증
        if (channelId == null) {
            log.error("channelId is null");
            throw new IllegalArgumentException("channelId는 필수입니다");
        }

        return messageRepository.findByChannelId(channelId, pageable)
                .map(MessageResponse::from);
    }

    /**
     * 메시지 수정 (Key 기반 도메인 조회, 조기 리턴)
     */
    @Transactional
    public MessageResponse updateMessage(Long messageId, MessageUpdateRequest request) {
        log.info("Updating message: messageId={}", messageId);

        // Step 1: Key 기반 도메인 조회
        Message message = findMessageById(messageId);

        // Step 2: 도메인 로직 실행 (도메인 내부에서 상태 검증)
        message.updateContent(request.getTitle(), request.getContent());

        log.info("Message updated successfully: messageId={}", messageId);

        return MessageResponse.from(message);
    }

    /**
     * 메시지 조회 (Key 기반)
     */
    private Message findMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.error("Message not found: messageId={}", messageId);
                    return new ResourceNotFoundException("Message", messageId);
                });
    }

    /**
     * 메시지 발행 준비 (DRAFT -> SCHEDULED)
     */
    @Transactional
    public void prepareForPublish(Long messageId) {
        log.info("Preparing message for publish: messageId={}", messageId);

        // Key 기반 도메인 조회
        Message message = findMessageById(messageId);

        // 도메인 로직 실행 (내부에서 상태 검증)
        message.prepareForPublish();

        log.info("Message prepared for publish: messageId={}", messageId);
    }

    /**
     * 메시지 발행 완료 (SCHEDULED -> PUBLISHED)
     */
    @Transactional
    public void markAsPublished(Long messageId) {
        log.info("Marking message as published: messageId={}", messageId);

        // Key 기반 도메인 조회
        Message message = findMessageById(messageId);

        // 도메인 로직 실행
        message.markAsPublished();

        log.info("Message marked as published: messageId={}", messageId);
    }

    /**
     * 메시지 취소
     */
    @Transactional
    public void cancelMessage(Long messageId) {
        log.info("Cancelling message: messageId={}", messageId);

        // Key 기반 도메인 조회
        Message message = findMessageById(messageId);

        // 도메인 로직 실행
        message.cancel();

        log.info("Message cancelled: messageId={}", messageId);
    }

    /**
     * 발행 예정 메시지 조회
     */
    public List<MessageResponse> getScheduledMessages() {
        return messageRepository.findScheduledMessages().stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 삭제 (조기 리턴 적용)
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        log.info("Deleting message: messageId={}", messageId);

        // Step 1: Key 기반 도메인 조회
        Message message = findMessageById(messageId);

        // Step 2: Early return - 발행 상태 검증
        if (message.getStatus() == MessageStatus.PUBLISHED) {
            log.warn("Cannot delete published message: messageId={}", messageId);
            throw new BusinessException("발행 완료된 메시지는 삭제할 수 없습니다");
        }

        // Step 3: 삭제 실행
        messageRepository.delete(message);

        log.info("Message deleted successfully: messageId={}", messageId);
    }
}
