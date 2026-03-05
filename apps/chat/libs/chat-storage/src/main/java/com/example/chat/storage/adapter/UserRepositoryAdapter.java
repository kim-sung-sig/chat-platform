package com.example.chat.storage.adapter;

import com.example.chat.domain.user.User;
import com.example.chat.domain.user.UserId;
import com.example.chat.domain.user.UserRepository;
import com.example.chat.storage.mapper.UserMapper;
import com.example.chat.storage.repository.JpaUserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserRepository 구현체 (Adapter)
 *
 * Hexagonal Architecture의 Persistence Adapter
 * - 도메인 포트(UserRepository)를 구현
 * - JPA 세부사항을 도메인으로부터 격리
 */
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        var entity = userMapper.toEntity(user);
        var saved = jpaUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        return jpaUserRepository.findById(id.value()).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UserId id) {
        return jpaUserRepository.existsById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
    }
}
