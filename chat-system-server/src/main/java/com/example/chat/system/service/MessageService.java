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
     * 메시지 생성
     */
    @Transactional
    public MessageResponse createMessage(MessageCreateRequest request) {
        log.info("Creating message for channel: {}", request.getChannelId());

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new ResourceNotFoundException("Channel", request.getChannelId()));

        if (!channel.getIsActive()) {
            throw new BusinessException("비활성화된 채널에는 메시지를 생성할 수 없습니다");
        }

        Message message = Message.builder()
                .channel(channel)
                .title(request.getTitle())
                .content(request.getContent())
                .messageType(request.getMessageType())
                .status(MessageStatus.DRAFT)
                .createdBy(request.getCreatedBy())
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message created successfully: {}", savedMessage.getId());

        return MessageResponse.from(savedMessage);
    }

    /**
     * 메시지 조회
     */
    public MessageResponse getMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        return MessageResponse.from(message);
    }

    /**
     * 채널별 메시지 목록 조회
     */
    public Page<MessageResponse> getMessagesByChannel(Long channelId, Pageable pageable) {
        return messageRepository.findByChannelId(channelId, pageable)
                .map(MessageResponse::from);
    }

    /**
     * 메시지 수정 (DRAFT 상태만 가능)
     */
    @Transactional
    public MessageResponse updateMessage(Long messageId, MessageUpdateRequest request) {
        log.info("Updating message: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        message.updateContent(request.getTitle(), request.getContent());
        log.info("Message updated successfully: {}", messageId);

        return MessageResponse.from(message);
    }

    /**
     * 메시지 발행 준비 (DRAFT -> SCHEDULED)
     */
    @Transactional
    public void prepareForPublish(Long messageId) {
        log.info("Preparing message for publish: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        message.prepareForPublish();
        log.info("Message prepared for publish: {}", messageId);
    }

    /**
     * 메시지 발행 완료 (SCHEDULED -> PUBLISHED)
     */
    @Transactional
    public void markAsPublished(Long messageId) {
        log.info("Marking message as published: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        message.markAsPublished();
        log.info("Message marked as published: {}", messageId);
    }

    /**
     * 메시지 취소
     */
    @Transactional
    public void cancelMessage(Long messageId) {
        log.info("Cancelling message: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        message.cancel();
        log.info("Message cancelled: {}", messageId);
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
     * 메시지 삭제
     */
    @Transactional
    public void deleteMessage(Long messageId) {
        log.info("Deleting message: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (message.getStatus() == MessageStatus.PUBLISHED) {
            throw new BusinessException("발행 완료된 메시지는 삭제할 수 없습니다");
        }

        messageRepository.delete(message);
        log.info("Message deleted: {}", messageId);
    }
}