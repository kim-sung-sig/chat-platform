package com.example.chat.storage.mapper;

import org.springframework.stereotype.Component;

import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.UserEntity;

/**
 * User Domain ↔ UserEntity 변환 Mapper
 */
@Component
public class UserMapper {

    /**
     * Domain → Entity 변환
     */
    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId().value())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastActiveAt(user.getLastActiveAt())
                .build();
    }

    /**
     * Entity → Domain 변환
     */
    public User toDomain(UserEntity entity) {
        return User.fromStorage(
                UserId.of(entity.getId()),
                entity.getUsername(),
                entity.getEmail(),
                "", // password는 storage layer에서 관리하지 않거나 별도 보안 엔티티 활용 권장
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt(),
                entity.getLastActiveAt());
    }
}
