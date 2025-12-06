package com.example.chat.storage.domain.message;

import com.example.chat.common.auth.model.UserId;

import java.util.List;
import java.util.Optional;

/**
 * MessageRead Repository 인터페이스
 */
public interface MessageReadRepository {

    /**
     * 메시지를 읽음으로 표시
     */
    boolean markRead(Long messageId, UserId userId);

    /**
     * 사용자가 메시지를 읽었는지 확인
     */
    boolean isReadBy(Long messageId, UserId userId);

    /**
     * 메시지를 읽은 사용자 ID 목록 조회
     */
    List<Long> findReaders(Long messageId);

    /**
     * 읽음 정보 ID 조회
     */
    Optional<Long> findReadId(Long messageId, UserId userId);
}
