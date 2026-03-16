package com.example.chat.voice.infrastructure.inmemory;

import com.example.chat.voice.domain.model.VoiceRoom;
import com.example.chat.voice.domain.repository.VoiceRoomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryVoiceRoomRepository implements VoiceRoomRepository {

    private final ConcurrentHashMap<String, VoiceRoom> store = new ConcurrentHashMap<>();

    @Override
    public Optional<VoiceRoom> findByChannelId(String channelId) {
        return Optional.ofNullable(store.get(channelId));
    }

    @Override
    public VoiceRoom save(VoiceRoom room) {
        store.put(room.getChannelId(), room);
        return room;
    }

    @Override
    public void deleteByChannelId(String channelId) {
        store.remove(channelId);
    }
}
