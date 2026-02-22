package com.example.chat.storage.adapter;

import com.example.chat.domain.friendship.Friendship;
import com.example.chat.domain.friendship.FriendshipId;
import com.example.chat.domain.friendship.FriendshipRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.mapper.FriendshipMapper;
import com.example.chat.storage.repository.JpaFriendshipRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FriendshipRepository 구현체 (Adapter)
 *
 * Hexagonal Architecture의 Adapter
 */
@Repository
public class FriendshipRepositoryAdapter implements FriendshipRepository {
    private final JpaFriendshipRepository jpaRepository;
    private final FriendshipMapper mapper;

    public FriendshipRepositoryAdapter(JpaFriendshipRepository jpaRepository, FriendshipMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Friendship save(Friendship friendship) {
        var entity = mapper.toEntity(friendship);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Friendship> findById(FriendshipId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Friendship> findByUserIdAndFriendId(UserId userId, UserId friendId) {
        return jpaRepository.findByUserIdAndFriendId(userId.value(), friendId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findAcceptedFriendsByUserId(UserId userId) {
        return jpaRepository.findAcceptedFriendsByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findPendingRequestsByFriendId(UserId friendId) {
        return jpaRepository.findPendingRequestsByFriendId(friendId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findPendingRequestsByUserId(UserId userId) {
        return jpaRepository.findPendingRequestsByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findBlockedByUserId(UserId userId) {
        return jpaRepository.findBlockedByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findFavoritesByUserId(UserId userId) {
        return jpaRepository.findFavoritesByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(FriendshipId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsMutualFriendship(UserId userId, UserId friendId) {
        return jpaRepository.existsMutualFriendship(userId.value(), friendId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> findAllByUserId(UserId userId) {
        return jpaRepository.findAllByUserId(userId.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
