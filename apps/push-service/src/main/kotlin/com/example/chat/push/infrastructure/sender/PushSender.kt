package com.example.chat.push.infrastructure.sender

interface PushSender {
    fun support(pushType: String): Boolean

    fun send(targetUserId: String, title: String, content: String)
}
