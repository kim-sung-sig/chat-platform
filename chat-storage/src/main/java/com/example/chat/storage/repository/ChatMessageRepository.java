// Overwrite empty file with correct repository definition
package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    Page<ChatMessageEntity> findByChannelIdOrderByCreatedAtDesc(String channelId, Pageable pageable);

    @Query("select m from ChatMessageEntity m where m.channelId = :channelId " +
            "and (m.createdAt < :createdAt or (m.createdAt = :createdAt and m.id < :id)) " +
            "order by m.createdAt desc, m.id desc")
    Page<ChatMessageEntity> findByChannelIdBefore(@Param("channelId") String channelId,
                                                  @Param("createdAt") OffsetDateTime createdAt,
                                                  @Param("id") Long id,
                                                  Pageable pageable);
}