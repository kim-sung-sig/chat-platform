package com.example.chat.storage.repository;

import com.example.chat.storage.entity.MessageReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaMessageReadRepository extends JpaRepository<MessageReadEntity, Long> {

    Optional<MessageReadEntity> findByMessageIdAndUserId(Long messageId, Long userId);

    @Query("select mr.userId from MessageReadEntity mr where mr.messageId = :messageId")
    List<Long> findUserIdsByMessageId(@Param("messageId") Long messageId);

    @Query("select mr.id from MessageReadEntity mr where mr.messageId = :messageId and mr.userId = :userId")
    Optional<Long> findIdByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO ms_message_read (message_id, user_id, read_at) VALUES (:messageId, :userId, NOW()) ON CONFLICT (user_id, message_id) DO NOTHING", nativeQuery = true)
    int insertIfNotExists(@Param("messageId") Long messageId, @Param("userId") Long userId);
}