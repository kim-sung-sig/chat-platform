package com.example.chat.push.application

import com.example.chat.push.domain.PushMessage
import com.example.chat.push.domain.PushMessageRepository
import com.example.chat.push.interfaces.kafka.NotificationEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushMessageService(private val pushMessageRepository: PushMessageRepository) {
    @Transactional
    fun savePushMessage(event: NotificationEvent): PushMessage {
        val pushMessage =
                PushMessage(
                        targetUserId = event.targetUserId,
                        title = event.title,
                        content = event.content,
                        pushType = event.pushType
                )
        return pushMessageRepository.save(pushMessage)
    }
}
