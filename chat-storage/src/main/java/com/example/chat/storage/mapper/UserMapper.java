package com.example.chat.storage.mapper;

import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * User Domain ↔ UserEntity 변환 Mapper
 */
@Component
public class UserMapper {

	/**
	 * Domain → Entity 변환
	 */
	public UserEntity toEntity(User user) {
		if (user == null) {
			return null;
		}

		return UserEntity.builder()
				.id(user.getId().getValue())
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
		if (entity == null) {
			return null;
		}

		return User.builder()
				.id(UserId.of(entity.getId()))
				.username(entity.getUsername())
				.email(entity.getEmail())
				.status(entity.getStatus())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.lastActiveAt(entity.getLastActiveAt())
				.build();
	}
}
