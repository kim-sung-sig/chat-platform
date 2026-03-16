package com.example.chat.voice.infrastructure.inmemory;

import com.example.chat.voice.domain.model.VoiceSignal;
import com.example.chat.voice.domain.repository.VoiceSignalRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryVoiceSignalRepository implements VoiceSignalRepository {

    private final Map<String, Map<String, List<VoiceSignal>>> store = new ConcurrentHashMap<>();

    @Override
    public VoiceSignal save(VoiceSignal signal) {
        store.computeIfAbsent(signal.getChannelId(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(signal.getToUserId(), key -> Collections.synchronizedList(new ArrayList<>()))
                .add(signal);
        return signal;
    }

    @Override
    public List<VoiceSignal> findByChannelIdAndToUserId(String channelId, String toUserId) {
        return new ArrayList<>(store.getOrDefault(channelId, Map.of())
                .getOrDefault(toUserId, List.of()));
    }

    @Override
    public void deleteAll(String channelId, String toUserId) {
        Map<String, List<VoiceSignal>> channelSignals = store.get(channelId);
        if (channelSignals != null) {
            channelSignals.remove(toUserId);
        }
    }
}
