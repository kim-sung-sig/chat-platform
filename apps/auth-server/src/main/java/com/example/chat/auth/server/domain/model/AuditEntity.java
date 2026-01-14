package com.example.chat.auth.server.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false) 
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false) 
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by") 
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at") 
    private Instant updatedAt;

}
