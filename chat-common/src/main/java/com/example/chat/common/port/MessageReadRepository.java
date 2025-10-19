package com.example.chat.common.port;

import com.example.chat.common.dto.UserId;

import java.util.List;
import java.util.Optional;

public interface MessageReadRepository {
    /**
     * Mark message as read by user. Returns true if a new read record was created, false if already existed.
     */
    boolean markRead(Long messageId, UserId userId);

    /**
     * Check if the message is read by the given user.
     */
    boolean isReadBy(Long messageId, UserId userId);

    /**
     * Return list of userIds who have read the message.
     */
    List<Long> findReaders(Long messageId);

    /**
     * Optionally fetch the raw read record id (if needed).
     */
    Optional<Long> findReadId(Long messageId, UserId userId);
}