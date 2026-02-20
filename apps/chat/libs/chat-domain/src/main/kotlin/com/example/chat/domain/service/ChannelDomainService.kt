package com.example.chat.domain.service

import com.example.chat.domain.channel.Channel
import com.example.chat.domain.channel.ChannelType
import com.example.chat.domain.user.User
import com.example.chat.domain.user.UserId

/**
 * 채널 도메인 서비스
 */
class ChannelDomainService {

    /**
     * 일대일 채팅 채널 생성
     *
     * Domain Rule:
     * - 두 사용자는 서로 다른 사용자여야 함
     * - 두 사용자 모두 활성 상태여야 함
     *
     * @param user1 첫 번째 사용자 (Aggregate Root)
     * @param user2 두 번째 사용자 (Aggregate Root)
     * @return 생성된 채널 (Aggregate Root)
     */
    fun createDirectChannel(user1: User, user2: User): Channel {
        if (user1.id == user2.id) throw DomainException("Cannot create direct channel with same user")
        if (!user1.canSendMessage()) throw DomainException("User1 is not in active status")
        if (!user2.canSendMessage()) throw DomainException("User2 is not in active status")

        // 채널 생성
        val channelName = generateDirectChannelName(user1.id, user2.id)
        val channel = Channel.create(channelName, ChannelType.DIRECT, user1.id)
        channel.addMember(user2.id)

        return channel
    }

    /**
     * 그룹 채팅 채널 생성
     *
     * @param name 채널명
     * @param owner 채널 소유자 (Aggregate Root)
     */
    fun createGroupChannel(name: String?, owner: User): Channel {
        // Early Return: 입력값 검증
        validateChannelName(name)
        if (!owner.canSendMessage()) throw DomainException("Owner is not in active status")
        return Channel.create(name!!, ChannelType.GROUP, owner.id)
    }

    /**
     * 공개 채널 생성
     */
    fun createPublicChannel(name: String?, owner: User): Channel {
        // Early Return: 입력값 검증
        validateChannelName(name)
        if (!owner.canSendMessage()) throw DomainException("Owner is not in active status")
        return Channel.create(name!!, ChannelType.PUBLIC, owner.id)
    }

    /**
     * 비공개 채널 생성
     */
    fun createPrivateChannel(name: String?, owner: User): Channel {
        // Early Return: 입력값 검증
        validateChannelName(name)
        if (!owner.canSendMessage()) throw DomainException("Owner is not in active status")
        return Channel.create(name!!, ChannelType.PRIVATE, owner.id)
    }

    /**
     * 채널에 멤버 추가
     *
     * Domain Rule:
     * - 채널은 활성 상태여야 함
     * - 사용자는 활성 상태여야 함
     * - 이미 멤버가 아니어야 함
     */
    fun addMemberToChannel(channel: Channel, user: User) {
        if (!channel.active) throw DomainException("Cannot add member to inactive channel")
        if (!user.canSendMessage()) throw DomainException("User is not in active status")
        if (channel.isMember(user.id)) throw DomainException("User is already a member of the channel")
        channel.addMember(user.id)
    }

    /**
     * 채널에서 멤버 제거
     *
     * Domain Rule:
     * - 채널 소유자는 제거할 수 없음
     * - 멤버여야 제거 가능
     */
    fun removeMemberFromChannel(channel: Channel, user: User) {
        if (channel.isOwner(user.id)) throw DomainException("Cannot remove channel owner")
        if (!channel.isMember(user.id)) throw DomainException("User is not a member of the channel")
        channel.removeMember(user.id)
    }

    // ============================================================
    // 입력값 검증 메서드
    // ============================================================

    /**
     * 채널명 검증
     */
    private fun validateChannelName(name: String?) {
        require(!name.isNullOrBlank()) { "Channel name cannot be null or blank" }

        // Early Return: 길이 제한 체크
        require(name.length <= 100) { "Channel name exceeds maximum length (100)" }
    }

    /**
     * 일대일 채팅 채널명 생성
     */
    private fun generateDirectChannelName(user1: UserId, user2: UserId): String {
        return "direct_${user1.value}_${user2.value}"
    }
}
