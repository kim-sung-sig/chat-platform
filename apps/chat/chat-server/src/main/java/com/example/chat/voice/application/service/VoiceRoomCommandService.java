package com.example.chat.voice.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.shared.exception.ResourceNotFoundException;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.voice.domain.model.VoiceRoom;
import com.example.chat.voice.domain.repository.VoiceRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceRoomCommandService {

    private final VoiceRoomRepository voiceRoomRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;

    public VoiceRoom join(String channelId, String userId) {
        validateChannel(channelId);
        validateMember(channelId, userId);

        VoiceRoom room = voiceRoomRepository.findByChannelId(channelId)
                .orElseGet(() -> voiceRoomRepository.save(new VoiceRoom(channelId, Instant.now())));

        room.addParticipant(userId, Instant.now());
        log.info("Voice join: channelId={}, userId={}", channelId, userId);
        return room;
    }

    public void leave(String channelId, String userId) {
        validateChannel(channelId);
        validateMember(channelId, userId);

        voiceRoomRepository.findByChannelId(channelId)
                .ifPresent(room -> {
                    room.removeParticipant(userId);
                    log.info("Voice leave: channelId={}, userId={}", channelId, userId);
                });
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
