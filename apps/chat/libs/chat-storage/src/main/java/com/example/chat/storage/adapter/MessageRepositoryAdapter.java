package com.example.chat.storage.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.common.Cursor;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.message.MessageRepository;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatMessageEntity;
import com.example.chat.storage.mapper.MessageMapper;
import com.example.chat.storage.repository.JpaChatMessageRepository;

import lombok.RequiredArgsConstructor;

/**
 * MessageRepository 구현체 (Adapter)
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

	private final JpaChatMessageRepository jpaRepository;
	private final MessageMapper mapper;

	@Override
	@Transactional
	public Message save(Message message) {
		ChatMessageEntity entity = Objects.requireNonNull(mapper.toEntity(message));
		ChatMessageEntity saved = jpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Message> findById(MessageId id) {
		return jpaRepository.findById(id.getValue())
				.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> findByChannelId(ChannelId channelId, Cursor cursor, int limit) {
		List<ChatMessageEntity> entities;

		if (cursor == null || cursor.isStart()) {
			// 커서가 없으면 최신 메시지부터 조회
			entities = jpaRepository.findByChannelIdOrderByCreatedAtDesc(
					channelId.getValue(),
					PageRequest.of(0, limit)).getContent();
		} else {
			// 커서 기반 페이징
			Instant cursorTime = parseCursor(cursor);
			entities = jpaRepository.findByChannelIdWithCursor(
					channelId.getValue(),
					cursorTime,
					PageRequest.of(0, limit)).getContent();
		}

		return entities.stream()
				.map(mapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> findBySenderId(UserId senderId, Cursor cursor, int limit) {
		List<ChatMessageEntity> entities = jpaRepository.findBySenderIdOrderByCreatedAtDesc(
				senderId.getValue());

		// 간단한 구현 (커서 처리는 생략)
		return entities.stream()
				.limit(limit)
				.map(mapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void delete(MessageId id) {
		jpaRepository.deleteById(id.getValue());
	}

	/**
	 * Cursor를 Instant로 파싱
	 */
	private Instant parseCursor(Cursor cursor) {
		try {
			return Instant.parse(cursor.getValue());
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid cursor format", e);
		}
	}
}
