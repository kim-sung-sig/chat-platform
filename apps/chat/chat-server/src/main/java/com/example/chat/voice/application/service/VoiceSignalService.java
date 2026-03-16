package com.example.chat.voice.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.shared.exception.ResourceNotFoundException;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.voice.domain.model.VoiceSignal;
import com.example.chat.voice.domain.model.VoiceSignalType;
import com.example.chat.voice.domain.repository.VoiceSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceSignalService {

    private final VoiceSignalRepository signalRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;

    public VoiceSignal send(String channelId, String fromUserId, String toUserId, VoiceSignalType type, String payload) {
        validateChannel(channelId);
        validateMember(channelId, fromUserId);
        validateMember(channelId, toUserId);

        if (toUserId == null || toUserId.isBlank() || payload == null || payload.isBlank()) {
            throw new ChatException(ChatErrorCode.VOICE_SIGNAL_INVALID);
        }

        VoiceSignal signal = new VoiceSignal(
                UUID.randomUUID().toString(),
                channelId,
                fromUserId,
                toUserId,
                type,
                payload,
                Instant.now()
        );

        log.info("Voice signal: channelId={}, from={}, to={}, type={}", channelId, fromUserId, toUserId, type);
        return signalRepository.save(signal);
    }

    public List<VoiceSignal> pull(String channelId, String userId) {
        validateChannel(channelId);
        validateMember(channelId, userId);

        List<VoiceSignal> signals = signalRepository.findByChannelIdAndToUserId(channelId, userId);
        signalRepository.deleteAll(channelId, userId);
        return signals;
    }

    private ChatChannelEntity validateChannel(String channelId) {
        ChatChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException(ChatErrorCode.CHANNEL_NOT_FOUND));
        if (!channel.isActive()) {
            throw new ChatException(ChatErrorCode.VOICE_CHANNEL_NOT_ACTIVE);
        }
        return channel;
    }

    private void validateMember(String channelId, String userId) {
        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            throw new ChatException(ChatErrorCode.VOICE_NOT_CHANNEL_MEMBER);
        }
    }
}
