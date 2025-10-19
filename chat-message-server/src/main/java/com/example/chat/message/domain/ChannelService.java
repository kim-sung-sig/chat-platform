package com.example.chat.message.domain;

import com.example.chat.storage.entity.ChatChannel;

public interface ChannelService {
    ChatChannel createChannel(String roomType);
    ChatChannel findById(Long id);
}