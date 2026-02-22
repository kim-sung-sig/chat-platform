package com.example.chat.domain.channel.metadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.user.UserId;

/**
 * 채팅방 메타데이터 Repository 인터페이스 (포트)
 */
public interface ChannelMetadataRepository {

    /**
     * 메타데이터 저장
     */
    ChannelMetadata save(ChannelMetadata metadata);

    /**
     * ID로 메타데이터 조회
     */
    Optional<ChannelMetadata> findById(ChannelMetadataId id);

    /**
     * 채널 ID와 사용자 ID로 조회
     */
    Optional<ChannelMetadata> findByChannelIdAndUserId(ChannelId channelId, UserId userId);

    /**
     * 사용자의 모든 채팅방 메타데이터 조회
     */
    List<ChannelMetadata> findByUserId(UserId userId);

    /**
     * 여러 채널의 메타데이터 배치 조회
     */
    Map<ChannelId, ChannelMetadata> findByChannelIdsAndUserId(List<ChannelId> channelIds, UserId userId);

    /**
     * 즐겨찾기 채팅방 메타데이터 조회
     */
    List<ChannelMetadata> findFavoritesByUserId(UserId userId);

    /**
     * 상단 고정 채팅방 메타데이터 조회
     */
    List<ChannelMetadata> findPinnedByUserId(UserId userId);

    /**
     * 읽지 않은 메시지가 있는 채팅방 메타데이터 조회
     */
    List<ChannelMetadata> findWithUnreadByUserId(UserId userId);

    /**
     * 메타데이터 삭제
     */
    void deleteById(ChannelMetadataId id);

    /**
     * 채널의 모든 메타데이터 삭제 (채널 삭제 시)
     */
    void deleteByChannelId(ChannelId channelId);

    /**
     * 메타데이터 존재 여부 확인
     */
    boolean existsByChannelIdAndUserId(ChannelId channelId, UserId userId);
}
