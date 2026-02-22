package com.example.chat.storage.repository;

import com.example.chat.domain.friendship.FriendshipStatus;
import com.example.chat.storage.entity.ChatFriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 JPA Repository
 */
public interface JpaFriendshipRepository extends JpaRepository<ChatFriendshipEntity, String> {

    /**
     * 두 사용자 간 친구 관계 조회
     */
    Optional<ChatFriendshipEntity> findByUserIdAndFriendId(String userId, String friendId);

    /**
     * 사용자의 수락된 친구 목록 조회
     */
    @Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'ACCEPTED' ORDER BY f.favorite DESC, f.updatedAt DESC")
    List<ChatFriendshipEntity> findAcceptedFriendsByUserId(@Param("userId") String userId);

    /**
     * 사용자에게 온 친구 요청 목록 조회
     */
    @Query("SELECT f FROM ChatFriendshipEntity f WHERE f.friendId = :friendId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
    List<ChatFriendshipEntity> findPendingRequestsByFriendId(@Param("friendId") String friendId);

    /**
     * 사용자가 보낸 친구 요청 목록 조회
     */
    @Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'PENDING' ORDER BY f.createdAt DESC")
    List<ChatFriendshipEntity> findPendingRequestsByUserId(@Param("userId") String userId);

    /**
     * 사용자가 차단한 목록 조회
     */
    @Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'BLOCKED' ORDER BY f.updatedAt DESC")
    List<ChatFriendshipEntity> findBlockedByUserId(@Param("userId") String userId);

    /**
     * 즐겨찾기 친구 목록 조회
     */
    @Query("SELECT f FROM ChatFriendshipEntity f WHERE f.userId = :userId AND f.status = 'ACCEPTED' AND f.favorite = true ORDER BY f.updatedAt DESC")
    List<ChatFriendshipEntity> findFavoritesByUserId(@Param("userId") String userId);

    /**
     * 양방향 친구 관계 존재 여부 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM ChatFriendshipEntity f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    boolean existsMutualFriendship(@Param("userId") String userId, @Param("friendId") String friendId);

    /**
     * 사용자의 모든 친구 관계 조회 (상태 무관)
     */
    List<ChatFriendshipEntity> findAllByUserId(String userId);

    /**
     * 상태별 친구 관계 조회
     */
    List<ChatFriendshipEntity> findByUserIdAndStatus(String userId, FriendshipStatus status);
}
