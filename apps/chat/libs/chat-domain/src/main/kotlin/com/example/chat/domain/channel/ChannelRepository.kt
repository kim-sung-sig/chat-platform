package com.example.chat.domain.channel

import com.example.chat.domain.user.UserId

/**
 * 채널 Repository 인터페이스 (포트)
 */
interface ChannelRepository {

    /**
     * 채널 저장
     */
    fun save(channel: Channel): Channel

    /**
     * ID로 채널 조회
     */
    fun findById(id: ChannelId): Channel?

    /**
     * 사용자가 속한 채널 목록 조회 (UserId)
     */
    fun findByMemberId(userId: UserId): List<Channel>

    /**
     * 사용자가 속한 채널 목록 조회 (String)
     */
    fun findByMemberId(userId: String): List<Channel>

    /**
     * 사용자가 소유한 채널 목록 조회
     */
    fun findByOwnerId(userId: UserId): List<Channel>

    /**
     * 공개 채널 목록 조회
     */
    fun findPublicChannels(): List<Channel>

    /**
     * 채널 삭제
     */
    fun delete(id: ChannelId)

    /**
     * 채널 존재 여부 확인
     */
    fun existsById(id: ChannelId): Boolean
}

