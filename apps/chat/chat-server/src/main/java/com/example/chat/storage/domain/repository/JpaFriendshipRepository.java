package com.example.chat.storage.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chat.common.core.enums.FriendshipStatus;
import com.example.chat.storage.domain.entity.ChatFriendshipEntity;

/**
 * 친구 관계 JPA Repository
 */
@Repository
public interface JpaFriendshipRepository extends JpaRepository<ChatFriendshipEntity, String> {

    /**
     * 두 사용자 간 친구 관계 조회
     */
    Optional<ChatFriendshipEntity> findByUserIdAndFriendId(String userId, String friendId);

    /**
     * 사용자의 수락된 친구 목록 조회 — favorite 내림차순, updatedAt 내림차순
     */
    List<ChatFriendshipEntity> findByUserIdAndStatusOrderByFavoriteDescUpdatedAtDesc(
            String userId, FriendshipStatus status);

    /**
     * 사용자에게 온 친구 요청 목록 조회 — createdAt 내림차순
     */
    List<ChatFriendshipEntity> findByFriendIdAndStatusOrderByCreatedAtDesc(
            String friendId, FriendshipStatus status);

    /**
     * 사용자가 보낸 친구 요청 목록 조회 — createdAt 내림차순
     */
    List<ChatFriendshipEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId, FriendshipStatus status);

    /**
     * 사용자가 차단한 목록 조회 — updatedAt 내림차순
     */
    List<ChatFriendshipEntity> findByUserIdAndStatusOrderByUpdatedAtDesc(
            String userId, FriendshipStatus status);

    /**
     * 즐겨찾기 친구 목록 조회 — ACCEPTED + favorite=true, updatedAt 내림차순
     */
    List<ChatFriendshipEntity> findByUserIdAndStatusAndFavoriteTrueOrderByUpdatedAtDesc(
            String userId, FriendshipStatus status);

    /**
     * [APPROVED @Query EXCEPTION]
     * Reason: bidirectional OR condition ((userId=A AND friendId=B) OR (userId=B AND friendId=A))
     * cannot be expressed as a single Spring Data named method.
     */
    @Query("SELECT COUNT(f) > 0 FROM ChatFriendshipEntity f " +
            "WHERE (f.userId = :userId AND f.friendId = :friendId) " +
            "OR (f.userId = :friendId AND f.friendId = :userId)")
    boolean existsMutualFriendship(@Param("userId") String userId,
                                   @Param("friendId") String friendId);

    /**
     * 사용자의 모든 친구 관계 조회 (상태 무관)
     */
    List<ChatFriendshipEntity> findAllByUserId(String userId);

    /**
     * 상태별 친구 관계 조회
     */
    List<ChatFriendshipEntity> findByUserIdAndStatus(String userId, FriendshipStatus status);
}
