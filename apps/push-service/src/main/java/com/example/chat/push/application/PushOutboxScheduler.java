package com.example.chat.push.application;

import com.example.chat.push.domain.PushMessage;
import com.example.chat.push.domain.PushMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 푸시 아웃박스 스케줄러
 *
 * 책임: 주기적으로 PENDING 상태의 푸시 메시지를 처리합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushOutboxScheduler {
    private final PushMessageRepository repository;
    private final PushProcessor processor;

    @Scheduled(fixedDelayString = "${push.scheduler.delay:5000}")
    @Transactional
    public void schedule() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(1);
        List<PushMessage> pendingMessages = repository.findPendingForProcessing(cutoff);

        if (!pendingMessages.isEmpty()) {
            log.info("Found {} pending push messages", pendingMessages.size());
        }

        for (PushMessage message : pendingMessages) {
            try {
                processor.process(message);
            } catch (Exception e) {
                log.error("Error processing push message {}", message.getId(), e);
            }
        }
    }
}
