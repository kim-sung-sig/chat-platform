package com.example.chat.auth.server.domain.model;

import org.springframework.util.StringUtils;

public record UserId(
    String userId
) {

    public UserId {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
    }

}
