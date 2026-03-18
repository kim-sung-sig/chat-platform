package com.example.chat.storage.domain.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.storage.domain.entity.ChatChannelEntity;

/**
 * 채널 JPA Repository.
 *
 * favorite / pinned / unread / keyword 필터를 DB 레벨에서 처리하여
 * 메모리 필터 제거 및 커서 페이징 정확도 보장.
 */
@Repository
public interface JpaChannelRepository extends JpaRepository<ChatChannelEntity, String> {

    /**
     * 채널 소유자 ID로 채널 목록 조회
     */
    List<ChatChannelEntity> findByOwnerId(String ownerId);

    /**
     * 공개 채널 목록 조회 — channelType = PUBLIC, active = true
     */
    List<ChatChannelEntity> findByChannelTypeAndActiveTrue(ChannelType channelType);

    /**
     * 특정 사용자가 멤버인 채널 목록을 DB 레벨 커서 페이징으로 조회.
     * channelType 필터 지원 (null 이면 전체).
     *
     * [APPROVED @Query EXCEPTION]
     * ChatChannelEntity 와 ChatChannelMemberEntity 는 서로 다른 aggregate root 이며
     * FK 연관관계(@OneToMany/@ManyToOne)가 없다. 따라서 named method 인
     * findByChannelMembers_UserId(...) 형태를 사용할 수 없고,
     * 두 테이블을 JOIN 하는 JPQL @Query 가 유일한 수단이다.
     *
     * @param userId          조회할 사용자 ID
     * @param channelType     채널 타입 문자열 (null 이면 전체)
     * @param cursorCreatedAt 커서 — 이 시각 이전에 생성된 채널만 반환 (null 이면 첫 페이지)
     * @param pageable        size 지정 전용 (sort 는 쿼리 내 ORDER BY 고정)
     */
    @Query("""
            SELECT c FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            WHERE m.userId = :userId
            AND (:channelType IS NULL OR CAST(c.channelType AS string) = :channelType)
            AND c.active = true
            AND (:cursorCreatedAt IS NULL OR c.createdAt < :cursorCreatedAt)
            ORDER BY c.createdAt DESC
            """)
    List<ChatChannelEntity> findByMemberIdWithFilters(
            @Param("userId") String userId,
            @Param("channelType") String channelType,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            Pageable pageable);

    /**
     * favorite/pinned/unread/keyword 를 DB 레벨에서 처리하는 풀 필터 커서 조회.
     *
     * [APPROVED @Query EXCEPTION]
     * 세 aggregate root(ChatChannelEntity, ChatChannelMemberEntity,
     * ChatChannelMetadataEntity) 간에 JPA 연관관계가 없다. 조건부 다중 술어
     * (onlyFavorites, onlyPinned, onlyUnread, keyword)를 named method 로
     * 표현하면 조합 폭발이 발생하며, @EntityGraph 로도 처리할 수 없다.
     * 따라서 3-테이블 JOIN + 동적 WHERE 를 포함하는 JPQL @Query 가 유일한 수단이다.
     *
     * @param userId          조회할 사용자 ID
     * @param channelType     채널 타입 문자열 (null 이면 전체)
     * @param onlyFavorites   즐겨찾기만 필터
     * @param onlyPinned      고정만 필터
     * @param onlyUnread      미읽음만 필터
     * @param keyword         채널명 키워드 검색 (null 이면 전체)
     * @param cursorCreatedAt 커서 — 이 시각 이전에 생성된 채널만 반환 (null 이면 첫 페이지)
     * @param pageable        size 지정 전용 (sort 는 쿼리 내 ORDER BY 고정)
     */
    @Query("""
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
            AND (:cursorCreatedAt IS NULL OR c.createdAt < :cursorCreatedAt)
            ORDER BY c.createdAt DESC
            """)
    List<ChatChannelEntity> findByMemberIdWithAllFilters(
            @Param("userId") String userId,
            @Param("channelType") String channelType,
            @Param("onlyFavorites") boolean onlyFavorites,
            @Param("onlyPinned") boolean onlyPinned,
            @Param("onlyUnread") boolean onlyUnread,
            @Param("keyword") String keyword,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            Pageable pageable);

    /**
     * 특정 사용자가 멤버인 채널 ID 목록 조회 (배치용).
     *
     * [APPROVED @Query EXCEPTION]
     * ChatChannelEntity 와 ChatChannelMemberEntity 간 JPA 연관관계가 없으므로
     * named method 로 표현할 수 없다. 멤버 테이블 JOIN 이 필수이며
     * @Query 가 유일한 수단이다.
     */
    @Query("""
            SELECT c.id FROM ChatChannelEntity c
            JOIN ChatChannelMemberEntity m ON m.channelId = c.id
            WHERE m.userId = :userId
            AND c.active = true
            """)
    List<String> findChannelIdsByMemberId(@Param("userId") String userId);
}
