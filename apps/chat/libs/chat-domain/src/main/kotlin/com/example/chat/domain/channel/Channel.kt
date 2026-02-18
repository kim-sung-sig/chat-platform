package com.example.chat.domain.channel

import com.example.chat.domain.user.UserId
import java.time.Instant

/**
 * 채널 Aggregate Root
 *
 * 책임:
 * - 채널 생명주기 관리
 * - 멤버 관리
 * - 채널 정보 유지
 */
class Channel private constructor(
    val id: ChannelId,
    private var _name: String,
    private var _description: String? = null,
    val type: ChannelType,
    val ownerId: UserId,
    private val _memberIds: MutableSet<UserId>,
    private var _active: Boolean,
    val createdAt: Instant,
    private var _updatedAt: Instant
) {
    val name: String get() = _name
    val description: String? get() = _description
    val memberIds: Set<UserId> get() = _memberIds.toSet()
    val active: Boolean get() = _active
    val updatedAt: Instant get() = _updatedAt

    /**
     * 멤버 추가
     */
    fun addMember(userId: UserId) {
        require(_active) { "Cannot add member to inactive channel" }
        require(!_memberIds.contains(userId)) { "User is already a member" }

        _memberIds.add(userId)
        _updatedAt = Instant.now()
    }

    /**
     * 멤버 제거
     */
    fun removeMember(userId: UserId) {
        require(userId != ownerId) { "Cannot remove channel owner" }
        require(_memberIds.contains(userId)) { "User is not a member" }

        _memberIds.remove(userId)
        _updatedAt = Instant.now()
    }

    /**
     * 멤버인지 확인
     */
    fun isMember(userId: UserId): Boolean = _memberIds.contains(userId)

    /**
     * 소유자인지 확인
     */
    fun isOwner(userId: UserId): Boolean = ownerId == userId

    /**
     * 채널 비활성화
     */
    fun deactivate() {
        _active = false
        _updatedAt = Instant.now()
    }

    /**
     * 채널 활성화
     */
    fun activate() {
        _active = true
        _updatedAt = Instant.now()
    }

    /**
     * 채널 정보 수정
     */
    fun updateInfo(name: String?, description: String?) {
        if (name != null && name.isNotBlank()) {
            _name = name
        }
        _description = description
        _updatedAt = Instant.now()
    }

    /**
     * 멤버 수 조회
     */
    val memberCount: Int get() = _memberIds.size

    companion object {
        /**
         * 새로운 채널 생성
         */
        fun create(name: String, type: ChannelType, ownerId: UserId): Channel {
            val memberIds = mutableSetOf(ownerId) // 생성자는 자동으로 멤버에 포함

            return Channel(
                id = ChannelId.generate(),
                _name = name,
                type = type,
                ownerId = ownerId,
                _memberIds = memberIds,
                _active = true,
                createdAt = Instant.now(),
                _updatedAt = Instant.now()
            )
        }

        /**
         * Storage Layer에서 재구성
         * Entity로부터 도메인 모델 복원 시 사용
         */
        @JvmStatic
        fun fromStorage(
            id: ChannelId,
            name: String,
            description: String?,
            type: ChannelType,
            ownerId: UserId,
            memberIds: Set<UserId>,
            active: Boolean,
            createdAt: Instant,
            updatedAt: Instant
        ): Channel {
            return Channel(
                id = id,
                _name = name,
                _description = description,
                type = type,
                ownerId = ownerId,
                _memberIds = memberIds.toMutableSet(),
                _active = active,
                createdAt = createdAt,
                _updatedAt = updatedAt
            )
        }
    }
}

