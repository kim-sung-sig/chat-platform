package com.example.chat.voice.domain.repository;

import com.example.chat.voice.domain.model.VoiceRoom;

import java.util.Optional;

public interface VoiceRoomRepository {
    Optional<VoiceRoom> findByChannelId(String channelId);

    VoiceRoom save(VoiceRoom room);

    void deleteByChannelId(String channelId);
}
