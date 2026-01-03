package com.example.chat.auth.server.domain.model;

import java.time.Instant;

public record Token(
		String token,
		Instant expiry
) {}
