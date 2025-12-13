package com.example.chat.storage.repository;

import com.example.chat.domain.channel.ChannelType;
import com.example.chat.storage.entity.ChatChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaChatChannelRepository extends JpaRepository<ChatChannelEntity, String> {

    /**
     * 소유자 ID로 채널 목록 조회
     */
    List<ChatChannelEntity> findByOwnerId(String ownerId);

    /**
     * 활성 채널만 조회
     */
    List<ChatChannelEntity> findByActiveTrue();

    /**
     * 타입과 활성 상태로 채널 조회
     */
    List<ChatChannelEntity> findByChannelTypeAndActive(ChannelType type, boolean active);
}
