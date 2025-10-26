package com.example.chat.system.domain.entity;

import com.example.chat.system.domain.enums.MessageStatus;
import com.example.chat.system.domain.enums.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Message 엔티티 단위 테스트
 */
class MessageTest {

    @Test
    void testPrepareForPublish_Success() {
        // Given
        Channel channel = Channel.builder()
                .channelName("Test Channel")
                .channelType("MARKETING")
                .isActive(true)
                .ownerId(1L)
                .build();

        Message message = Message.builder()
                .channel(channel)
                .title("Test Message")
                .content("Test Content")
                .messageType(MessageType.TEXT)
                .status(MessageStatus.DRAFT)
                .createdBy(1L)
                .build();

        // When
        message.prepareForPublish();

        // Then
        assertEquals(MessageStatus.SCHEDULED, message.getStatus());
    }

    @Test
    void testPrepareForPublish_ThrowsExceptionWhenNotDraft() {
        // Given
        Channel channel = Channel.builder()
                .channelName("Test Channel")
                .channelType("MARKETING")
                .isActive(true)
                .ownerId(1L)
                .build();

        Message message = Message.builder()
                .channel(channel)
                .title("Test Message")
                .content("Test Content")
                .messageType(MessageType.TEXT)
                .status(MessageStatus.PUBLISHED)
                .createdBy(1L)
                .build();

        // When & Then
        assertThrows(IllegalStateException.class, message::prepareForPublish);
    }

    @Test
    void testMarkAsPublished_Success() {
        // Given
        Channel channel = Channel.builder()
                .channelName("Test Channel")
                .channelType("MARKETING")
                .isActive(true)
                .ownerId(1L)
                .build();

        Message message = Message.builder()
                .channel(channel)
                .title("Test Message")
                .content("Test Content")
                .messageType(MessageType.TEXT)
                .status(MessageStatus.SCHEDULED)
                .createdBy(1L)
                .build();

        // When
        message.markAsPublished();

        // Then
        assertEquals(MessageStatus.PUBLISHED, message.getStatus());
    }

    @Test
    void testUpdateContent_Success() {
        // Given
        Channel channel = Channel.builder()
                .channelName("Test Channel")
                .channelType("MARKETING")
                .isActive(true)
                .ownerId(1L)
                .build();

        Message message = Message.builder()
                .channel(channel)
                .title("Original Title")
                .content("Original Content")
                .messageType(MessageType.TEXT)
                .status(MessageStatus.DRAFT)
                .createdBy(1L)
                .build();

        // When
        message.updateContent("Updated Title", "Updated Content");

        // Then
        assertEquals("Updated Title", message.getTitle());
        assertEquals("Updated Content", message.getContent());
    }

    @Test
    void testCancel_Success() {
        // Given
        Channel channel = Channel.builder()
                .channelName("Test Channel")
                .channelType("MARKETING")
                .isActive(true)
                .ownerId(1L)
                .build();

        Message message = Message.builder()
                .channel(channel)
                .title("Test Message")
                .content("Test Content")
                .messageType(MessageType.TEXT)
                .status(MessageStatus.SCHEDULED)
                .createdBy(1L)
                .build();

        // When
        message.cancel();

        // Then
        assertEquals(MessageStatus.CANCELLED, message.getStatus());
    }
}