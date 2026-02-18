package com.example.chat.storage.mapper

import com.example.chat.domain.user.User
import com.example.chat.domain.user.UserId
import com.example.chat.storage.entity.UserEntity
import org.springframework.stereotype.Component

/**
 * User Domain ↔ UserEntity 변환 Mapper
 */
@Component
class UserMapper {

    /**
     * Domain → Entity 변환
     */
    fun toEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id.value,
            username = user.username,
            email = user.email,
            status = user.status,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastActiveAt = user.lastActiveAt
        )
    }

    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: UserEntity): User {
        return User.fromStorage(
            id = UserId.of(entity.id),
            username = entity.username,
            email = entity.email,
            password = "", // password는 storage layer에서 관리하지 않음
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt ?: entity.createdAt,
            lastActiveAt = entity.lastActiveAt
        )
    }
}

