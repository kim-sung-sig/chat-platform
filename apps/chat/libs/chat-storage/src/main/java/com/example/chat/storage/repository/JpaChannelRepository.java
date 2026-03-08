package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatChannelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채널 JPA Repository
 *
 * favorite / pinned / unread / keyword 필터를 DB 레벨에서 처리하여
 * 메모리 필터 제거 및 페이징 정확도 보장
 */
@Repository
public interface JpaChannelRepository extends JpaRepository<ChatChannelEntity, String> {

    /**
     * 채널 소유자 ID로 채널 목록 조회
     */
    List<ChatChannelEntity> findByOwnerId(String ownerId);

    /**
     * 공개 채널 목록 조회
     */
    @Query("SELECT c FROM ChatChannelEntity c WHERE c.channelType = 'PUBLIC' AND c.active = true")
    List<ChatChannelEntity> findPublicChannels();

    /**
     * 특정 사용자가 멤버인 채널 목록을 DB 레벨 페이징으로 조회
     * channelType 필터 지원 (null 이면 전체)
     */
    @Query("""
            SELECT c FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            WHERE m.userId = :userId
            AND (:channelType IS NULL OR CAST(c.channelType AS string) = :channelType)
            AND c.active = true
            """)
    Page<ChatChannelEntity> findByMemberIdWithFilters(
            @Param("userId") String userId,
            @Param("channelType") String channelType,
            Pageable pageable);

    /**
     * favorite/pinned/unread/keyword 를 DB 레벨에서 처리하는 풀 필터 조회
     * - onlyFavorites: metadata.favorite = true
     * - onlyPinned: metadata.pinned = true
     * - onlyUnread: metadata.unreadCount > 0
     * - keyword: 채널명 ILIKE 또는 (DM 상대 탐색은 서비스 레이어)
     */
    @Query(value = """
            SELECT c FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            LEFT JOIN ChatChannelMetadataEntity meta ON meta.channelId = c.id AND meta.userId = :userId
            WHERE m.userId = :userId
            AND c.active = true
            AND (:channelType IS NULL OR CAST(c.channelType AS string) = :channelType)
            AND (:onlyFavorites = false OR meta.favorite = true)
            AND (:onlyPinned = false OR meta.pinned = true)
            AND (:onlyUnread = false OR (meta.unreadCount IS NOT NULL AND meta.unreadCount > 0))
            AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(c) FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            LEFT JOIN ChatChannelMetadataEntity meta ON meta.channelId = c.id AND meta.userId = :userId
            WHERE m.userId = :userId
            AND c.active = true
            AND (:channelType IS NULL OR CAST(c.channelType AS string) = :channelType)
            AND (:onlyFavorites = false OR meta.favorite = true)
            AND (:onlyPinned = false OR meta.pinned = true)
            AND (:onlyUnread = false OR (meta.unreadCount IS NOT NULL AND meta.unreadCount > 0))
            AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ChatChannelEntity> findByMemberIdWithAllFilters(
            @Param("userId") String userId,
            @Param("channelType") String channelType,
            @Param("onlyFavorites") boolean onlyFavorites,
            @Param("onlyPinned") boolean onlyPinned,
            @Param("onlyUnread") boolean onlyUnread,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 특정 사용자가 멤버인 채널 ID 목록 조회 (배치용)
     */
    @Query("""
            SELECT c.id FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            WHERE m.userId = :userId
            AND c.active = true
            """)
    List<String> findChannelIdsByMemberId(@Param("userId") String userId);
}
