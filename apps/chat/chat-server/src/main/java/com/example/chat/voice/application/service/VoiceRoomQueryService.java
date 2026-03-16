package com.example.chat.voice.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.shared.exception.ResourceNotFoundException;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.voice.domain.model.VoiceParticipant;
import com.example.chat.voice.domain.model.VoiceRoom;
import com.example.chat.voice.domain.repository.VoiceRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoiceRoomQueryService {

    private final VoiceRoomRepository voiceRoomRepository;
    private final JpaChannelRepository channelRepository;
    private final JpaChannelMemberRepository channelMemberRepository;

    public List<VoiceParticipant> listParticipants(String channelId, String userId) {
        validateChannel(channelId);
        validateMember(channelId, userId);

        VoiceRoom room = voiceRoomRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.VOICE_ROOM_NOT_FOUND));

        return room.getParticipants();
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
