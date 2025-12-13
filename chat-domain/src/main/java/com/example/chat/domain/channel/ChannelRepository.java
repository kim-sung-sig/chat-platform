package com.example.chat.domain.channel;

import com.example.chat.domain.user.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 채널 Repository 인터페이스 (포트)
 */
public interface ChannelRepository {

    /**
     * 채널 저장
     */
    Channel save(Channel channel);

    /**
     * ID로 채널 조회
     */
    Optional<Channel> findById(ChannelId id);

    /**
     * 사용자가 속한 채널 목록 조회 (UserId)
     */
    List<Channel> findByMemberId(UserId userId);

    /**
     * 사용자가 속한 채널 목록 조회 (String)
     */
    List<Channel> findByMemberId(String userId);

    /**
     * 사용자가 소유한 채널 목록 조회
     */
    List<Channel> findByOwnerId(UserId userId);

    /**
     * 공개 채널 목록 조회
     */
    List<Channel> findPublicChannels();

    /**
     * 채널 삭제
     */
    void delete(ChannelId id);

    /**
     * 채널 존재 여부 확인
     */
    boolean existsById(ChannelId id);
}
