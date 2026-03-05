package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatChannelMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 채널 멤버 JPA Repository
 */
@Repository
public interface JpaChannelMemberRepository extends JpaRepository<ChatChannelMemberEntity, Long> {

    /**
     * 채널 ID로 멤버 목록 조회
     */
    List<ChatChannelMemberEntity> findByChannelId(String channelId);

    /**
     * 사용자 ID로 소속 채널 ID 목록 조회
     */
    List<ChatChannelMemberEntity> findByUserId(String userId);

    /**
     * 채널 ID 목록으로 멤버 전체 조회
     */
    List<ChatChannelMemberEntity> findByChannelIdIn(List<String> channelIds);

    /**
     * 채널에서 특정 멤버 삭제
     */
    void deleteByChannelIdAndUserId(String channelId, String userId);

    /**
     * 채널의 모든 멤버 삭제
     */
    void deleteByChannelId(String channelId);
}
