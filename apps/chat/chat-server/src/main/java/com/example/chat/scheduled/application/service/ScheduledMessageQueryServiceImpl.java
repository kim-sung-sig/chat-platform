package com.example.chat.scheduled.application.service;

import com.example.chat.scheduled.domain.repository.ScheduledMessageRepository;
import com.example.chat.scheduled.rest.dto.response.ScheduledMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 예약 발송 Query 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledMessageQueryServiceImpl implements ScheduledMessageQueryService {

    private final ScheduledMessageRepository scheduleRepository;

    @Override
    public List<ScheduledMessageResponse> listScheduledMessages(String channelId, String senderId) {
        return scheduleRepository.findByChannelIdAndSenderIdOrderByScheduledAtDesc(channelId, senderId)
                .stream()
                .map(ScheduledMessageResponse::from)
                .toList();
    }
}
