package com.example.chat.system.service;

import com.example.chat.system.domain.entity.Customer;
import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.entity.MessageHistory;
import com.example.chat.system.domain.enums.PublishStatus;
import com.example.chat.system.dto.response.CursorPageResponse;
import com.example.chat.system.dto.response.MessageHistoryResponse;
import com.example.chat.system.exception.ResourceNotFoundException;
import com.example.chat.system.repository.CustomerRepository;
import com.example.chat.system.repository.MessageHistoryRepository;
import com.example.chat.system.repository.MessageRepository;
import com.example.chat.system.repository.ScheduleRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메시지 발행 이력 관리 서비스
 * 책임: 발행 이력 조회, 통계, 재시도 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageHistoryService {

    private final MessageHistoryRepository messageHistoryRepository;
    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ScheduleRuleRepository scheduleRuleRepository;

    /**
     * 메시지별 발행 이력 조회 (커서 기반 페이징)
     */
    public CursorPageResponse<MessageHistoryResponse> getHistoriesByMessage(
            Long messageId, Long cursor, Integer size) {
        log.info("Fetching message histories for message: {}, cursor: {}, size: {}", messageId, cursor, size);

        // 메시지 존재 확인
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("Message", messageId);
        }

        Pageable pageable = PageRequest.of(0, size + 1); // 다음 페이지 확인을 위해 +1
        List<MessageHistory> histories = messageHistoryRepository
                .findByMessageIdWithCursor(messageId, cursor, pageable);

        return buildCursorResponse(histories, size);
    }

    /**
     * 고객별 발행 이력 조회 (커서 기반 페이징)
     */
    public CursorPageResponse<MessageHistoryResponse> getHistoriesByCustomer(
            Long customerId, Long cursor, Integer size) {
        log.info("Fetching message histories for customer: {}, cursor: {}, size: {}", customerId, cursor, size);

        // 고객 존재 확인
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", customerId);
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<MessageHistory> histories = messageHistoryRepository
                .findByCustomerIdWithCursor(customerId, cursor, pageable);

        return buildCursorResponse(histories, size);
    }

    /**
     * 스케줄별 발행 이력 조회 (커서 기반 페이징)
     */
    public CursorPageResponse<MessageHistoryResponse> getHistoriesByScheduleRule(
            Long scheduleRuleId, Long cursor, Integer size) {
        log.info("Fetching message histories for schedule: {}, cursor: {}, size: {}", scheduleRuleId, cursor, size);

        // 스케줄 존재 확인
        if (!scheduleRuleRepository.existsById(scheduleRuleId)) {
            throw new ResourceNotFoundException("ScheduleRule", scheduleRuleId);
        }

        Pageable pageable = PageRequest.of(0, size + 1);
        List<MessageHistory> histories = messageHistoryRepository
                .findByScheduleRuleIdWithCursor(scheduleRuleId, cursor, pageable);

        return buildCursorResponse(histories, size);
    }

    /**
     * 발행 이력 생성
     */
    @Transactional
    public MessageHistory createHistory(Message message, Customer customer, Long scheduleRuleId) {
        log.info("Creating message history for message: {}, customer: {}", message.getId(), customer.getId());

        MessageHistory history = MessageHistory.builder()
                .message(message)
                .customer(customer)
                .publishStatus(PublishStatus.PENDING)
                .retryCount(0)
                .scheduleRuleId(scheduleRuleId)
                .build();

        return messageHistoryRepository.save(history);
    }

    /**
     * 발행 성공 처리
     */
    @Transactional
    public void markAsSuccess(Long historyId) {
        MessageHistory history = messageHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageHistory", historyId));

        history.markAsSuccess();
        log.info("Message history marked as success: {}", historyId);
    }

    /**
     * 발행 실패 처리
     */
    @Transactional
    public void markAsFailed(Long historyId, String errorMessage) {
        MessageHistory history = messageHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageHistory", historyId));

        history.markAsFailed(errorMessage);
        log.error("Message history marked as failed: {}, error: {}", historyId, errorMessage);
    }

    /**
     * 재시도 처리
     */
    @Transactional
    public void markAsRetry(Long historyId, String errorMessage) {
        MessageHistory history = messageHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageHistory", historyId));

        history.markAsRetry(errorMessage);
        log.warn("Message history marked for retry: {}, error: {}", historyId, errorMessage);
    }

    /**
     * 재시도 대상 조회
     */
    public List<MessageHistory> getRetryTargets(int maxRetryCount) {
        return messageHistoryRepository.findRetryTargets(maxRetryCount);
    }

    /**
     * 커서 기반 응답 생성
     */
    private CursorPageResponse<MessageHistoryResponse> buildCursorResponse(
            List<MessageHistory> histories, Integer size) {

        boolean hasNext = histories.size() > size;
        List<MessageHistory> content = hasNext ? histories.subList(0, size) : histories;

        List<MessageHistoryResponse> responses = content.stream()
                .map(MessageHistoryResponse::from)
                .collect(Collectors.toList());

        Long nextCursor = hasNext && !content.isEmpty()
                ? content.get(content.size() - 1).getId()
                : null;

        return CursorPageResponse.of(responses, nextCursor, hasNext, size);
    }
}