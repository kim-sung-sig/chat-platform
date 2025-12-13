package com.example.chat.storage.adapter;

import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.storage.entity.UserEntity;
import com.example.chat.storage.mapper.UserMapper;
import com.example.chat.storage.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserRepository 구현체 (Adapter)
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JpaUserRepository jpaRepository;
	private final UserMapper mapper;

	@Override
	@Transactional
	public User save(User user) {
		UserEntity entity = mapper.toEntity(user);
		UserEntity saved = jpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(UserId id) {
		return jpaRepository.findById(id.getValue())
				.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsById(UserId id) {
		return jpaRepository.existsById(id.getValue());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByUsername(String username) {
		return jpaRepository.findByUsername(username)
				.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByEmail(String email) {
		return jpaRepository.findByEmail(email)
				.map(mapper::toDomain);
	}
}
