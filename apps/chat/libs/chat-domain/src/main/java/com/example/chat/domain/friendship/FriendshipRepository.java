package com.example.chat.domain.friendship;

import java.util.List;
import java.util.Optional;

import com.example.chat.domain.user.UserId;

/**
 * 친구 관계 Repository 인터페이스 (포트)
 *
 * Hexagonal Architecture의 Port
 */
public interface FriendshipRepository {

    /**
     * 친구 관계 저장
     */
    Friendship save(Friendship friendship);

    /**
     * ID로 친구 관계 조회
     */
    Optional<Friendship> findById(FriendshipId id);

    /**
     * 두 사용자 간 친구 관계 조회
     */
    Optional<Friendship> findByUserIdAndFriendId(UserId userId, UserId friendId);

    /**
     * 사용자의 모든 친구 목록 조회 (수락된 관계만)
     */
    List<Friendship> findAcceptedFriendsByUserId(UserId userId);

    /**
     * 사용자에게 온 친구 요청 목록 조회
     */
    List<Friendship> findPendingRequestsByFriendId(UserId friendId);

    /**
     * 사용자가 보낸 친구 요청 목록 조회
     */
    List<Friendship> findPendingRequestsByUserId(UserId userId);

    /**
     * 사용자가 차단한 목록 조회
     */
    List<Friendship> findBlockedByUserId(UserId userId);

    /**
     * 즐겨찾기 친구 목록 조회
     */
    List<Friendship> findFavoritesByUserId(UserId userId);

    /**
     * 친구 관계 삭제
     */
    void deleteById(FriendshipId id);

    /**
     * 양방향 친구 관계 존재 여부 확인
     */
    boolean existsMutualFriendship(UserId userId, UserId friendId);

    /**
     * 사용자의 모든 친구 관계 조회 (상태 무관)
     */
    List<Friendship> findAllByUserId(UserId userId);
}
