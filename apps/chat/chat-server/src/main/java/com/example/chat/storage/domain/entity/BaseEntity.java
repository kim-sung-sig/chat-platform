package com.example.chat.storage.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

/**
 * 공통 JPA Lifecycle 슈퍼클래스.
 * createdAt / updatedAt 설정을 중앙화하여 엔티티 간 중복을 제거한다.
 * id 필드는 포함하지 않는다 — 각 엔티티가 PK 전략을 직접 선언한다.
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}
