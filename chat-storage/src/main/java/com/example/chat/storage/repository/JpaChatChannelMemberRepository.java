package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatChannelMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaChatChannelMemberRepository extends JpaRepository<ChatChannelMemberEntity, Long> {

    /**
     * 특정 채널의 모든 멤버 조회
     */
    List<ChatChannelMemberEntity> findByChannelId(String channelId);

    /**
     * 특정 사용자가 속한 채널 ID 목록 조회
     */
    @Query("SELECT m.channelId FROM ChatChannelMemberEntity m WHERE m.userId = :userId")
    List<String> findChannelIdsByUserId(@Param("userId") String userId);

    /**
     * 특정 채널의 특정 사용자 멤버십 존재 여부
     */
    boolean existsByChannelIdAndUserId(String channelId, String userId);

    /**
     * 특정 채널의 특정 사용자 멤버십 삭제
     */
    void deleteByChannelIdAndUserId(String channelId, String userId);
}
