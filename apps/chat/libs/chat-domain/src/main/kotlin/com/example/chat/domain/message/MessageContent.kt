package com.example.chat.domain.message

/**
 * 메시지 내용 (Value Object)
 */
data class MessageContent(
    val text: String? = null,
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null
) {
    /**
     * 내용이 비어있는지 확인
     */
    fun isEmpty(): Boolean = text.isNullOrBlank() && mediaUrl.isNullOrBlank()

    companion object {
        /**
         * 텍스트 메시지 생성
         */
        fun text(text: String): MessageContent {
            require(text.isNotBlank()) { "Text content cannot be null or blank" }
            return MessageContent(text = text)
        }

        /**
         * 이미지 메시지 생성
         */
        fun image(mediaUrl: String, fileName: String? = null, fileSize: Long? = null): MessageContent {
            require(mediaUrl.isNotBlank()) { "Media URL cannot be null or blank" }
            return MessageContent(
                mediaUrl = mediaUrl,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = "image/*"
            )
        }

        /**
         * 파일 메시지 생성
         */
        fun file(mediaUrl: String, fileName: String, fileSize: Long? = null, mimeType: String): MessageContent {
            require(mediaUrl.isNotBlank()) { "Media URL cannot be null or blank" }
            require(fileName.isNotBlank()) { "File name cannot be null or blank" }
            return MessageContent(
                mediaUrl = mediaUrl,
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType
            )
        }
    }
}

