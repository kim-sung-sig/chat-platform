package com.example.chat.storage.adapter;

import com.example.chat.storage.repository.ChatChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
//public class DomainChannelRepositoryAdapter implements ChannelRepository {
public class DomainChannelRepositoryAdapter {

    private final ChatChannelRepository delegate;

//    @Override
//    public ChatChannel save(ChatChannel channel) {
//        com.example.chat.storage.entity.ChatChannel entity = new com.example.chat.storage.entity.ChatChannel();
//        entity.setRoomType(channel.getRoomType());
//        com.example.chat.storage.entity.ChatChannel saved = delegate.save(entity);
//        return new ChatChannel(saved.getId(), saved.getRoomType(), saved.getCreatedAt());
//    }
}