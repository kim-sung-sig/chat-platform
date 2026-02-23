package com.example.chat.push.application;

import com.example.chat.push.domain.PushMessage;
import com.example.chat.push.domain.PushMessageRepository;
import com.example.chat.push.infrastructure.kafka.PushResultEvent;
import com.example.chat.push.infrastructure.kafka.PushResultProducer;
import com.example.chat.push.infrastructure.sender.PushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 푸시 메시지 처리기
 *
 * 책임: 개별 푸시 메시지를 처리하고 결과를 발행합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushProcessor {
    private final PushMessageRepository repository;
    private final List<PushSender> senders;
    private final PushResultProducer pushResultProducer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(PushMessage message) {
        log.info("Processing push message: {}", message.getId());

        try {
            message.markProcessing();
            repository.saveAndFlush(message);

            PushSender sender = senders.stream()
                    .filter(s -> s.support(message.getPushType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No sender found for type: " + message.getPushType()));

            sender.send(message.getTargetUserId(), message.getTitle(), message.getContent());
            message.markCompleted();

        } catch (Exception e) {
            log.error("Failed to send push message: {}", message.getId(), e);
            message.markFailed(e.getMessage());
        } finally {
            repository.save(message);
            publishResult(message);
        }
    }

    private void publishResult(PushMessage message) {
        try {
            pushResultProducer.sendResult(new PushResultEvent(
                    message.getId(),
                    message.getTargetUserId(),
                    message.getStatus().name(),
                    message.getErrorMessage()));
        } catch (Exception e) {
            log.error("Failed to republish push result for message {}", message.getId(), e);
        }
    }
}
