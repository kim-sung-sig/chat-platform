package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채널 Repository
 */
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /**
     * 활성화된 채널 조회
     */
    List<Channel> findByIsActiveTrue();

    /**
     * 채널 타입으로 조회
     */
    List<Channel> findByChannelType(String channelType);

    /**
     * 소유자 ID로 채널 조회
     */
    List<Channel> findByOwnerId(Long ownerId);

    /**
     * 채널명으로 검색 (LIKE)
     */
    @Query("SELECT c FROM Channel c WHERE c.channelName LIKE %:keyword% AND c.isActive = true")
    List<Channel> searchByChannelName(@Param("keyword") String keyword);

    /**
     * 채널 ID와 활성화 상태로 조회
     */
    Optional<Channel> findByIdAndIsActiveTrue(Long id);
}