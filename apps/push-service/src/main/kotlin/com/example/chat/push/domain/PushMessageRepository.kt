package com.example.chat.push.domain

import jakarta.persistence.LockModeType
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface PushMessageRepository : JpaRepository<PushMessage, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT p FROM PushMessage p WHERE p.status = 'PENDING' AND p.createdAt < :cutoff ORDER BY p.createdAt ASC"
    )
    fun findPendingForProcessing(cutoff: LocalDateTime): List<PushMessage>
}
