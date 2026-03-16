package com.example.chat.voice.domain.repository;

import com.example.chat.voice.domain.model.VoiceSignal;

import java.util.List;

public interface VoiceSignalRepository {
    VoiceSignal save(VoiceSignal signal);

    List<VoiceSignal> findByChannelIdAndToUserId(String channelId, String toUserId);

    void deleteAll(String channelId, String toUserId);
}
