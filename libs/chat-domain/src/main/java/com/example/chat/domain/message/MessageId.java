package com.example.chat.domain.message;

import java.util.UUID;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 메시지 ID (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class MessageId {
	@NonNull
	private final String value;

	private MessageId(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("MessageId cannot be null or blank");
		}
		this.value = value;
	}

	public static MessageId of(String value) {
		return new MessageId(value);
	}

	public static MessageId generate() {
		return new MessageId(UUID.randomUUID().toString());
	}
}
