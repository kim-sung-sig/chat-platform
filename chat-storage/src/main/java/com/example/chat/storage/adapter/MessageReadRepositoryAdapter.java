package com.example.chat.storage.adapter;

import com.example.chat.common.port.MessageReadRepository;
import com.example.chat.storage.entity.MessageReadEntity;
import com.example.chat.storage.repository.SpringDataMessageReadRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class MessageReadRepositoryAdapter implements MessageReadRepository {

    private final SpringDataMessageReadRepository repo;

    public MessageReadRepositoryAdapter(SpringDataMessageReadRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean markRead(Long messageId, Long userId) {
        int affected = repo.insertIfNotExists(messageId, userId);
        return affected > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReadBy(Long messageId, Long userId) {
        return repo.findByMessageIdAndUserId(messageId, userId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findReaders(Long messageId) {
        return repo.findUserIdsByMessageId(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findReadId(Long messageId, Long userId) {
        return repo.findIdByMessageIdAndUserId(messageId, userId);
    }
}