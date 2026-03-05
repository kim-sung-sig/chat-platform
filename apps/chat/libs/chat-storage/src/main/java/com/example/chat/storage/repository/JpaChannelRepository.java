package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채널 JPA Repository
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
}
