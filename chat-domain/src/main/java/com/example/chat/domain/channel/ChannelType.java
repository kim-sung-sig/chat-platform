package com.example.chat.domain.channel;

/**
 * 채널 타입
 */
public enum ChannelType {
    /**
     * 일대일 채팅
     */
    DIRECT,

    /**
     * 그룹 채팅
     */
    GROUP,

    /**
     * 공개 채널
     */
    PUBLIC,

    /**
     * 비공개 채널
     */
    PRIVATE
}
