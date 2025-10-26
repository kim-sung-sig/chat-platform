package com.example.chat.system.service;

import com.example.chat.system.domain.entity.Customer;
import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.entity.MessageHistory;
import com.example.chat.system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 메시지 발행 서비스
 * 책임: 실제 메시지 발행 처리, Virtual Thread 기반 비동기 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisherService {

    private final CustomerRepository customerRepository;
    private final MessageHistoryService messageHistoryService;

    // Virtual Thread Executor (Java 21)
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 메시지 발행 (채널 구독자 전체)
     */
    @Transactional
    public void publishMessage(Message message, Long scheduleRuleId) {
        log.info("Publishing message: {} for channel: {}", message.getId(), message.getChannel().getId());

        // 채널 구독자 조회
        List<Customer> subscribers = customerRepository
                .findSubscribedCustomersByChannelId(message.getChannel().getId());

        if (subscribers.isEmpty()) {
            log.warn("No subscribers found for channel: {}", message.getChannel().getId());
            return;
        }

        log.info("Found {} subscribers for channel: {}", subscribers.size(), message.getChannel().getId());

        // 각 고객에게 비동기로 발행
        List<CompletableFuture<Void>> futures = subscribers.stream()
                .map(customer -> CompletableFuture.runAsync(
                        () -> publishToCustomer(message, customer, scheduleRuleId),
                        virtualThreadExecutor
                ))
                .toList();

        // 모든 발행 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Message publishing completed: {}", message.getId());
    }

    /**
     * 개별 고객에게 메시지 발행
     */
    private void publishToCustomer(Message message, Customer customer, Long scheduleRuleId) {
        MessageHistory history = null;

        try {
            // 발행 이력 생성
            history = messageHistoryService.createHistory(message, customer, scheduleRuleId);

            // 실제 메시지 발행 로직 (외부 시스템 호출 등)
            sendMessage(message, customer);

            // 성공 처리
            messageHistoryService.markAsSuccess(history.getId());
            log.debug("Message published successfully to customer: {}", customer.getId());

        } catch (Exception e) {
            log.error("Failed to publish message to customer: {}", customer.getId(), e);

            if (history != null) {
                // 재시도 가능 여부 확인
                if (history.canRetry(3)) {
                    messageHistoryService.markAsRetry(history.getId(), e.getMessage());
                } else {
                    messageHistoryService.markAsFailed(history.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 실제 메시지 전송 (외부 시스템 연동)
     * TODO: 실제 메시징 시스템(Kafka, RabbitMQ 등) 연동
     */
    private void sendMessage(Message message, Customer customer) {
        // 시뮬레이션: 메시지 전송
        log.debug("Sending message [{}] to customer [{}] via email: {}",
                message.getTitle(), customer.getCustomerName(), customer.getEmail());

        // 실제 구현 시:
        // - Kafka Producer로 메시지 발행
        // - RabbitMQ로 메시지 전송
        // - 외부 API 호출 등

        // 시뮬레이션을 위한 지연
        try {
            Thread.sleep(100); // 100ms 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Message sending interrupted", e);
        }
    }

    /**
     * 재시도 대상 메시지 재발행
     */
    @Transactional
    public void retryFailedMessages() {
        log.info("Retrying failed messages");

        List<MessageHistory> retryTargets = messageHistoryService.getRetryTargets(3);

        if (retryTargets.isEmpty()) {
            log.info("No messages to retry");
            return;
        }

        log.info("Found {} messages to retry", retryTargets.size());

        retryTargets.forEach(history -> {
            try {
                sendMessage(history.getMessage(), history.getCustomer());
                messageHistoryService.markAsSuccess(history.getId());
                log.info("Retry successful for history: {}", history.getId());
            } catch (Exception e) {
                log.error("Retry failed for history: {}", history.getId(), e);

                if (history.canRetry(3)) {
                    messageHistoryService.markAsRetry(history.getId(), e.getMessage());
                } else {
                    messageHistoryService.markAsFailed(history.getId(), e.getMessage());
                }
            }
        });
    }
}