package com.example.chat.message.domain.impl;

import com.example.chat.message.domain.ChannelService;
import com.example.chat.storage.entity.ChatChannel;
import com.example.chat.storage.repository.ChatChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채널 도메인 서비스 구현. 채널 생성/조회 등의 도메인 책임을 담당합니다.
 * TODO: channel metadata, permissions, validation 로직 보강
 */
@Service
public class ChannelServiceImpl implements ChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    private final ChatChannelRepository chatChannelRepository;

    public ChannelServiceImpl(ChatChannelRepository chatChannelRepository) {
        this.chatChannelRepository = chatChannelRepository;
    }

    @Override
    @Transactional
    public ChatChannel createChannel(String roomType) {
        ChatChannel channel = new ChatChannel();
        channel.setRoomType(roomType != null ? roomType : "DEFAULT");
        // createdAt handled by entity's @PrePersist or default
        ChatChannel saved = chatChannelRepository.save(channel);
        logger.info("Created channel id={} type={}", saved.getId(), saved.getRoomType());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatChannel findById(Long id) {
        return chatChannelRepository.findById(id).orElse(null);
    }
}