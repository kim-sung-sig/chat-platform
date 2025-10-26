package com.example.chat.system.repository;

import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메시지 Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 채널 ID로 메시지 조회 (페이징)
     */
    Page<Message> findByChannelId(Long channelId, Pageable pageable);

    /**
     * 메시지 상태로 조회
     */
    List<Message> findByStatus(MessageStatus status);

    /**
     * 채널 ID와 상태로 조회
     */
    List<Message> findByChannelIdAndStatus(Long channelId, MessageStatus status);


    /**
     * 작성자 ID로 조회 (페이징)
     */
    Page<Message> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * 제목 검색 (LIKE, 페이징)
     */
    @Query("SELECT m FROM Message m WHERE m.title LIKE %:keyword% ORDER BY m.createdAt DESC")
    Page<Message> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 발행 대기 중인 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.status = 'SCHEDULED' ORDER BY m.createdAt ASC")
    List<Message> findScheduledMessages();
}