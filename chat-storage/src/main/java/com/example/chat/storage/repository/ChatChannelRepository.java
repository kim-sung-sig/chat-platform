package com.example.chat.storage.repository;

import com.example.chat.storage.entity.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {
}