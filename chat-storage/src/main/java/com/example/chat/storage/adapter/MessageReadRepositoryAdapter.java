package com.example.chat.storage.adapter;

import com.example.chat.common.dto.UserId;
import com.example.chat.common.port.MessageReadRepository;
import com.example.chat.storage.repository.JpaMessageReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageReadRepositoryAdapter implements MessageReadRepository {

    private final JpaMessageReadRepository repo;

    @Override
    @Transactional
    public boolean markRead(Long messageId, UserId userId) {
        int affected = repo.insertIfNotExists(messageId, userId.get());
        return affected > 0;
    }

    @Override
    public boolean isReadBy(Long messageId, UserId userId) {
        return repo.findByMessageIdAndUserId(messageId, userId.get()).isPresent();
    }

    @Override
    public List<Long> findReaders(Long messageId) {
        return repo.findUserIdsByMessageId(messageId);
    }

    @Override
    public Optional<Long> findReadId(Long messageId, UserId userId) {
        return repo.findIdByMessageIdAndUserId(messageId, userId.get());
    }
}