package com.example.chat.push.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "push_messages")
class PushMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val targetUserId: String,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PushStatus = PushStatus.PENDING,

    @Column(nullable = false)
    val pushType: String, // e.g., TOAST, FCM, SLACK

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var processedAt: LocalDateTime? = null,

    @Column
    var errorMessage: String? = null,

    @Version
    var version: Long = 0
)

enum class PushStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
