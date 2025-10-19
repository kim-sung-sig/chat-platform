package com.example.chat.common.dto;

import java.util.function.Supplier;

public record UserId(
		Long value
) implements Supplier<Long> {

	public UserId {}

	public static UserId of(Long value) {
		if (value == null || value <= 0)
			throw new IllegalArgumentException("UserId must be positive");
		return new UserId(value);
	}

	@Override
	public Long get() {
		return value;
	}

}