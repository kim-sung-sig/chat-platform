package com.example.chat.system.infrastructure.scheduler;

import com.example.chat.system.service.MessagePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 메시지 재시도 스케줄러
 * 실패한 메시지를 주기적으로 재시도
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRetryScheduler {

    private final MessagePublisherService messagePublisherService;

    /**
     * 1분마다 실패한 메시지 재시도
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void retryFailedMessages() {
        log.info("Starting scheduled retry for failed messages");

        try {
            messagePublisherService.retryFailedMessages();
            log.info("Scheduled retry completed");
        } catch (Exception e) {
            log.error("Error during scheduled retry", e);
        }
    }
}