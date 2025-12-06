package com.example.chat.message.domain.service;

import com.example.chat.storage.domain.message.Message;
import com.example.chat.storage.domain.message.MessageRepository;
import com.example.chat.storage.handler.MessageHandler;
import com.example.chat.storage.handler.MessageHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메시지 도메인 서비스
 * 도메인 로직 실행 및 영속화 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageDomainService {

    private final MessageRepository messageRepository;
    private final MessageHandlerRegistry handlerRegistry;

    /**
     * 메시지 처리 및 저장 (Key 기반 도메인 조회 패턴)
     */
    @Transactional
    public Message processAndSave(Message message) {
        log.debug("Processing message: type={}", message.getMessageType());

        // Step 1: Handler를 통한 전처리
        processBeforeSave(message);

        // Step 2: 도메인 로직 실행 (메시지 발송)
        Message sentMessage = executeSendDomain(message);

        // Step 3: 영속화
        Message savedMessage = saveMessage(sentMessage);

        // Step 4: Handler를 통한 후처리
        processAfterSave(savedMessage);

        log.debug("Message processed successfully: messageId={}", savedMessage.getId());

        return savedMessage;
    }

    /**
     * 메시지 조회 (Key 기반)
     */
    public Message findById(Long messageId) {
        return messageRepository.findById(messageId).orElse(null);
    }

    /**
     * 메시지 존재 여부 확인
     */
    public boolean existsById(Long messageId) {
        return messageRepository.existsById(messageId);
    }

    // ========== Private Helper Methods ==========

    /**
     * 도메인 로직 실행: 메시지 발송
     */
    private Message executeSendDomain(Message message) {
        return message.send();
    }

    /**
     * 영속화
     */
    private Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    /**
     * Handler를 통한 전처리
     */
    private void processBeforeSave(Message message) {
        MessageHandler handler = findHandlerByType(message);
        handler.processBeforeSave(message);
    }

    /**
     * Handler를 통한 후처리
     */
    private void processAfterSave(Message message) {
        MessageHandler handler = findHandlerByType(message);
        handler.processAfterSave(message);
    }

    /**
     * Key 기반 Handler 조회
     */
    private MessageHandler findHandlerByType(Message message) {
        return handlerRegistry.getHandler(message.getMessageType());
    }
}
