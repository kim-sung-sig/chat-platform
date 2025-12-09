package com.example.chat.domain.service;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelType;
import com.example.chat.domain.user.UserId;

/**
 * 채널 도메인 서비스
 */
public class ChannelDomainService {

    /**
     * 일대일 채팅 채널 생성
     */
    public Channel createDirectChannel(UserId user1, UserId user2) {
        if (user1.equals(user2)) {
            throw new IllegalArgumentException("Cannot create direct channel with same user");
        }

        String channelName = generateDirectChannelName(user1, user2);
        Channel channel = Channel.create(channelName, ChannelType.DIRECT, user1);
        channel.addMember(user2);

        return channel;
    }

    /**
     * 그룹 채팅 채널 생성
     */
    public Channel createGroupChannel(String name, UserId ownerId) {
        validateChannelName(name);
        return Channel.create(name, ChannelType.GROUP, ownerId);
    }

    /**
     * 공개 채널 생성
     */
    public Channel createPublicChannel(String name, UserId ownerId) {
        validateChannelName(name);
        return Channel.create(name, ChannelType.PUBLIC, ownerId);
    }

    /**
     * 비공개 채널 생성
     */
    public Channel createPrivateChannel(String name, UserId ownerId) {
        validateChannelName(name);
        return Channel.create(name, ChannelType.PRIVATE, ownerId);
    }

    /**
     * 채널명 검증
     */
    private void validateChannelName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Channel name cannot be null or blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Channel name exceeds maximum length (100)");
        }
    }

    /**
     * 일대일 채팅 채널명 생성
     */
    private String generateDirectChannelName(UserId user1, UserId user2) {
        return "direct_" + user1.getValue() + "_" + user2.getValue();
    }
}
