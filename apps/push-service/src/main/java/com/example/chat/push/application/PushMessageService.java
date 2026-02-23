package com.example.chat.push.application;

import com.example.chat.push.domain.PushMessage;
import com.example.chat.push.domain.PushMessageRepository;
import com.example.chat.push.interfaces.kafka.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 푸시 메시지 저장 서비스
 *
 * 책임: Kafka 이벤트를 받아 PushMessage 도메인 객체를 저장합니다.
 */
@Service
@RequiredArgsConstructor
public class PushMessageService {
    private final PushMessageRepository pushMessageRepository;

    @Transactional
    public PushMessage savePushMessage(NotificationEvent event) {
        PushMessage pushMessage = PushMessage.of(
                event.targetUserId(),
                event.title(),
                event.content(),
                event.pushType());
        return pushMessageRepository.save(pushMessage);
    }
}
