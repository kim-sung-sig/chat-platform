package com.example.chat.domain.service

import com.example.chat.domain.channel.Channel
import com.example.chat.domain.message.Message
import com.example.chat.domain.message.MessageContent
import com.example.chat.domain.message.MessageType
import com.example.chat.domain.user.User

/**
 * 메시지 도메인 서비스
 *
 * DDD Domain Service의 역할:
 * 1. 여러 Aggregate Root 간의 협력을 조율
 * 2. 복잡한 도메인 규칙 검증 (단일 Aggregate으로 표현할 수 없는 규칙)
 * 3. 도메인 불변식(Invariants) 보장
 *
 * 이 서비스는 Channel + User Aggregate의 협력을 통해
 * 메시지 발송 가능 여부를 검증하고 Message를 생성합니다.
 */
class MessageDomainService {

    /**
     * 텍스트 메시지 생성
     *
     * Domain Service의 핵심:
     * - Channel Aggregate: 채널 상태 및 멤버십 검증
     * - User Aggregate: 사용자 상태 및 권한 검증
     * - Message Aggregate: 메시지 생성
     *
     * @param channel 메시지를 발송할 채널 (Aggregate Root)
     * @param sender 메시지를 발송하는 사용자 (Aggregate Root)
     * @param text 메시지 텍스트 내용
     * @return 생성된 메시지 (Aggregate Root)
     * @throws DomainException 도메인 규칙 위반 시
     */
    fun createTextMessage(channel: Channel, sender: User, text: String): Message {
        // Early Return: 텍스트 내용 사전 검증
        validateTextContent(text)

        // Domain Rule: Channel + User 협력을 통한 발송 권한 검증
        validateMessageSendingPermission(channel, sender)

        // Message 생성 (Aggregate 생성)
        val content = MessageContent.text(text)
        return Message.create(channel.id, sender.id, content, MessageType.TEXT)
    }

    /**
     * 이미지 메시지 생성
     */
    fun createImageMessage(
        channel: Channel,
        sender: User,
        mediaUrl: String,
        fileName: String? = null,
        fileSize: Long? = null
    ): Message {
        // Early Return: 입력값 사전 검증
        validateMediaUrl(mediaUrl)
        fileSize?.let { validateImageFileSize(it) }

        // Domain Rule: 발송 권한 검증
        validateMessageSendingPermission(channel, sender)

        // Message 생성
        val content = MessageContent.image(mediaUrl, fileName, fileSize)
        return Message.create(channel.id, sender.id, content, MessageType.IMAGE)
    }

    /**
     * 파일 메시지 생성
     */
    fun createFileMessage(
        channel: Channel,
        sender: User,
        mediaUrl: String,
        fileName: String,
        fileSize: Long? = null,
        mimeType: String
    ): Message {
        // Early Return: 입력값 사전 검증
        validateMediaUrl(mediaUrl)
        validateFileName(fileName)
        fileSize?.let { validateFileSize(it) }

        // Domain Rule: 발송 권한 검증
        validateMessageSendingPermission(channel, sender)

        // Message 생성
        val content = MessageContent.file(mediaUrl, fileName, fileSize, mimeType)
        return Message.create(channel.id, sender.id, content, MessageType.FILE)
    }

    /**
     * 시스템 메시지 생성 (관리자/시스템용)
     *
     * 시스템 메시지는 User 검증이 불필요
     */
    fun createSystemMessage(channel: Channel, text: String): Message {
        // Early Return: 입력값 검증
        validateTextContent(text)

        // Early Return: 채널 상태 검증
        require(channel.active) { "Cannot send system message to inactive channel" }

        // 시스템 계정으로 메시지 생성
        val content = MessageContent.text(text)
        return Message.create(channel.id, User.SYSTEM_USER_ID, content, MessageType.SYSTEM)
    }

    // ============================================================
    // 도메인 규칙 검증 메서드 (Domain Validation Logic)
    // ============================================================

    /**
     * 메시지 발송 권한 검증 (핵심 도메인 규칙)
     *
     * 복합 도메인 규칙:
     * 1. Channel: 활성 상태여야 함
     * 2. Channel: 사용자가 멤버여야 함
     * 3. User: 활성 상태여야 함 (차단/정지 아님)
     */
    private fun validateMessageSendingPermission(channel: Channel, sender: User) {
        // Early Return: 채널 활성화 확인
        require(channel.active) { "Channel is not active" }

        // Early Return: 채널 멤버십 확인
        require(channel.isMember(sender.id)) { "User is not a member of the channel" }

        // Early Return: 사용자 차단 여부 확인
        require(!sender.isBanned()) { "User is banned and cannot send messages" }

        // Early Return: 사용자 정지 여부 확인
        require(!sender.isSuspended()) { "User is suspended and cannot send messages" }

        // Early Return: 사용자 메시지 발송 가능 여부 확인
        require(sender.canSendMessage()) {
            "User is not allowed to send messages (status: ${sender.status})"
        }
    }

    // ============================================================
    // 입력값 검증 메서드 (Input Validation)
    // ============================================================

    /**
     * 텍스트 내용 검증
     */
    private fun validateTextContent(text: String) {
        // Early Return: null/blank 체크
        require(text.isNotBlank()) { "Text content cannot be null or blank" }

        // Early Return: 길이 제한 체크
        require(text.length <= 5000) { "Text content exceeds maximum length (5000 characters)" }
    }

    /**
     * 미디어 URL 검증
     */
    private fun validateMediaUrl(mediaUrl: String) {
        // Early Return
        require(mediaUrl.isNotBlank()) { "Media URL cannot be null or blank" }
    }

    /**
     * 파일명 검증
     */
    private fun validateFileName(fileName: String) {
        // Early Return: null/blank 체크
        require(fileName.isNotBlank()) { "File name cannot be null or blank" }

        // Early Return: 길이 제한 체크
        require(fileName.length <= 255) { "File name is too long (max 255 characters)" }
    }

    /**
     * 이미지 파일 크기 검증 (10MB 제한)
     */
    private fun validateImageFileSize(fileSize: Long) {
        // Early Return: 음수 체크
        require(fileSize > 0) { "File size must be positive" }

        // Early Return: 크기 제한 체크
        val maxImageSize = 10 * 1024 * 1024L // 10MB
        require(fileSize <= maxImageSize) { "Image file size exceeds maximum allowed size (10MB)" }
    }

    /**
     * 파일 크기 검증 (50MB 제한)
     */
    private fun validateFileSize(fileSize: Long) {
        // Early Return: 음수 체크
        require(fileSize > 0) { "File size must be positive" }

        // Early Return: 크기 제한 체크
        val maxFileSize = 50 * 1024 * 1024L // 50MB
        require(fileSize <= maxFileSize) { "File size exceeds maximum allowed size (50MB)" }
    }
}

