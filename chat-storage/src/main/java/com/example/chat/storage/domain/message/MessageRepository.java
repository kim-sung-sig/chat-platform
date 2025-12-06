package com.example.chat.storage.domain.message;

import java.util.List;
import java.util.Optional;

/**
 * Message Repository 인터페이스
 * 도메인 레이어의 리포지토리 정의
 */
public interface MessageRepository {

    /**
     * 메시지 저장
     */
    Message save(Message message);

    /**
     * 메시지 ID로 조회
     */
    Optional<Message> findById(Long messageId);

    /**
     * 채팅방의 메시지 목록 조회 (Cursor 기반)
     * @param roomId 채팅방 ID
     * @param cursor 커서 (마지막 메시지 ID)
     * @param limit 조회 개수
     * @return 메시지 목록
     */
    List<Message> findByRoomIdWithCursor(String roomId, Long cursor, int limit);

    /**
     * 채널의 메시지 목록 조회 (Cursor 기반)
     * @param channelId 채널 ID
     * @param cursor 커서 (마지막 메시지 ID)
     * @param limit 조회 개수
     * @return 메시지 목록
     */
    List<Message> findByChannelIdWithCursor(String channelId, Long cursor, int limit);

    /**
     * 메시지 삭제
     */
    void delete(Long messageId);

    /**
     * 메시지 존재 여부 확인
     */
    boolean existsById(Long messageId);
}
