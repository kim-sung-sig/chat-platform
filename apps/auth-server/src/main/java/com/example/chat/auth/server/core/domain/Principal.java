package com.example.chat.auth.server.core.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 인증 대상(주체) - JPA Entity
 * - User, ServiceAccount 등
 * - 도메인 = 엔티티 (간단한 모델이므로 분리하지 않음)
 */
@Entity
@Table(name = "principals")
public class Principal {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "identifier", unique = true, nullable = false)
    private String identifier;  // email, username, etc

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PrincipalType type;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Principal() {
        // JPA
    }

    public Principal(UUID id, String identifier, PrincipalType type, boolean active) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.identifier = Objects.requireNonNull(identifier, "identifier cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.active = active;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public PrincipalType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Principal principal = (Principal) o;
        return Objects.equals(id, principal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Principal{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", type=" + type +
                ", active=" + active +
                '}';
    }
}
