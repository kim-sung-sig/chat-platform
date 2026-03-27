package com.example.chat.storage.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chat.storage.domain.entity.ChatChannelMetadataEntity;

/**
 * 채팅방 메타데이터 JPA Repository
 */
@Repository
public interface JpaChannelMetadataRepository extends JpaRepository<ChatChannelMetadataEntity, String> {

    /**
     * 채널 ID와 사용자 ID로 조회
     */
    Optional<ChatChannelMetadataEntity> findByChannelIdAndUserId(String channelId, String userId);

    /**
     * 사용자의 모든 메타데이터 조회
     */
    List<ChatChannelMetadataEntity> findByUserId(String userId);

    /**
     * 여러 채널의 메타데이터 배치 조회
     */
    List<ChatChannelMetadataEntity> findByChannelIdInAndUserId(List<String> channelIds, String userId);

    /**
     * 즐겨찾기 메타데이터 조회 — lastActivityAt 내림차순
     */
    List<ChatChannelMetadataEntity> findByUserIdAndFavoriteTrueOrderByLastActivityAtDesc(String userId);

    /**
     * 상단 고정 메타데이터 조회 — lastActivityAt 내림차순
     */
    List<ChatChannelMetadataEntity> findByUserIdAndPinnedTrueOrderByLastActivityAtDesc(String userId);

    /**
     * 읽지 않은 메시지가 있는 메타데이터 조회 — lastActivityAt 내림차순
     */
    List<ChatChannelMetadataEntity> findByUserIdAndUnreadCountGreaterThanOrderByLastActivityAtDesc(
            String userId, int unreadCount);

    /**
     * 채널의 모든 메타데이터 삭제
     */
    void deleteByChannelId(String channelId);

    /**
     * 특정 사용자의 특정 채널 메타데이터 삭제 (멤버 퇴장 시)
     */
    void deleteByChannelIdAndUserId(String channelId, String userId);

    /**
     * 존재 여부 확인
     */
    boolean existsByChannelIdAndUserId(String channelId, String userId);

    /**
     * 발신자를 제외한 모든 채널 멤버의 unreadCount 일괄 증가.
     * 단일 UPDATE로 N번 개별 호출을 대체 (쓰기 폭발 방지).
     */
    @Modifying
    @Query("UPDATE ChatChannelMetadataEntity m "
            + "SET m.unreadCount = m.unreadCount + 1, m.lastActivityAt = CURRENT_TIMESTAMP "
            + "WHERE m.channelId = :channelId AND m.userId != :senderId")
    int bulkIncrementUnreadCount(@Param("channelId") String channelId,
                                  @Param("senderId") String senderId);

    /**
     * 발신자 메타데이터의 lastActivityAt 갱신 (발송 시 활동 기록).
     */
    @Modifying
    @Query("UPDATE ChatChannelMetadataEntity m "
            + "SET m.lastActivityAt = CURRENT_TIMESTAMP "
            + "WHERE m.channelId = :channelId AND m.userId = :userId")
    int updateLastActivity(@Param("channelId") String channelId,
                            @Param("userId") String userId);
}
