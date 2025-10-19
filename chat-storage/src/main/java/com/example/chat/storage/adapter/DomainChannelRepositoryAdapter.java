package com.example.chat.storage.adapter;

import com.example.chat.domain.entity.ChatChannel;
import com.example.chat.domain.repository.ChannelRepository;
import com.example.chat.storage.entity.ChatChannel as ChatChannelEntity;
import com.example.chat.storage.repository.ChatChannelRepository;
import org.springframework.stereotype.Component;

@Component
public class DomainChannelRepositoryAdapter implements ChannelRepository {

    private final ChatChannelRepository delegate;

    public DomainChannelRepositoryAdapter(ChatChannelRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatChannel save(ChatChannel channel) {
        com.example.chat.storage.entity.ChatChannel entity = new com.example.chat.storage.entity.ChatChannel();
        entity.setRoomType(channel.getRoomType());
        com.example.chat.storage.entity.ChatChannel saved = delegate.save(entity);
        return new ChatChannel(saved.getId(), saved.getRoomType(), saved.getCreatedAt());
    }
}