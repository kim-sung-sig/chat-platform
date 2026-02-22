package com.example.chat.domain.message;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.common.Cursor;
import com.example.chat.domain.user.UserId;

/**
 * 메시지 Repository 인터페이스 (포트)
 */
public interface MessageRepository {

    /**
     * 메시지 저장
     */
    Message save(Message message);

    /**
     * ID로 메시지 조회
     */
    Optional<Message> findById(MessageId id);

    /**
     * 채널의 메시지 목록 조회 (커서 기반 페이징)
     */
    List<Message> findByChannelId(ChannelId channelId, Cursor cursor, int limit);

    /**
     * 특정 사용자가 보낸 메시지 목록 조회
     */
    List<Message> findBySenderId(UserId senderId, Cursor cursor, int limit);

    /**
     * 메시지 삭제
     */
    void delete(MessageId id);

    /**
     * 여러 채널의 마지막 메시지 배치 조회
     */
    Map<ChannelId, Message> findLastMessageByChannelIds(List<ChannelId> channelIds);
}
