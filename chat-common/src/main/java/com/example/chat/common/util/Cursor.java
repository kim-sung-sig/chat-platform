package com.example.chat.common.util;

import java.time.OffsetDateTime;

public final class Cursor {
    private final OffsetDateTime createdAt;
    private final Long id;

    public Cursor(OffsetDateTime createdAt, Long id) {
        this.createdAt = createdAt;
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }
}