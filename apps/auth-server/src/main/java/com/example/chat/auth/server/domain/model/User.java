package com.example.chat.auth.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Slf4j
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "auth_user")
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_user_id_gen")
    @SequenceGenerator(name = "auth_user_id_gen", sequenceName = "auth_user_id_seq", allocationSize = 1)
    private Long id;

    // security
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "login_fail_count")
    private Integer loginFailCount;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "temp_password")
    private String tempPassword;

    @Column(name = "temp_password_expired_at")
    private Instant tempPasswordExpiredAt;

    // 사용자 정보
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    // audit
    @Column(name = "created_by", updatable = false) @CreatedBy
    private String createdBy;

    @Column(name = "created_at", updatable = false) @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_by") @LastModifiedBy
    private String updatedBy;

    @Column(name = "updated_at") @LastModifiedDate
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.loginFailCount == null) this.loginFailCount = 0;
    }
}
