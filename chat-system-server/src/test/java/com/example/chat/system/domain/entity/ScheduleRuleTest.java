package com.example.chat.system.domain.entity;

import com.example.chat.system.domain.enums.ScheduleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScheduleRule 엔티티 단위 테스트
 */
class ScheduleRuleTest {

    @Test
    void testCanExecute_ActiveSchedule() {
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
                .messageType(com.example.chat.system.domain.enums.MessageType.TEXT)
                .status(com.example.chat.system.domain.enums.MessageStatus.SCHEDULED)
                .createdBy(1L)
                .build();

        ScheduleRule scheduleRule = ScheduleRule.builder()
                .message(message)
                .scheduleType(ScheduleType.ONCE)
                .executionTime(LocalDateTime.now().minusHours(1))
                .nextExecutionTime(LocalDateTime.now().minusHours(1))
                .isActive(true)
                .jobName("TEST_JOB")
                .jobGroup("TEST_GROUP")
                .executionCount(0)
                .build();

        // When
        boolean canExecute = scheduleRule.canExecute();

        // Then
        assertTrue(canExecute);
    }

    @Test
    void testCanExecute_InactiveSchedule() {
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
                .messageType(com.example.chat.system.domain.enums.MessageType.TEXT)
                .status(com.example.chat.system.domain.enums.MessageStatus.SCHEDULED)
                .createdBy(1L)
                .build();

        ScheduleRule scheduleRule = ScheduleRule.builder()
                .message(message)
                .scheduleType(ScheduleType.ONCE)
                .executionTime(LocalDateTime.now().minusHours(1))
                .nextExecutionTime(LocalDateTime.now().minusHours(1))
                .isActive(false)
                .jobName("TEST_JOB")
                .jobGroup("TEST_GROUP")
                .executionCount(0)
                .build();

        // When
        boolean canExecute = scheduleRule.canExecute();

        // Then
        assertFalse(canExecute);
    }

    @Test
    void testMarkAsExecuted() {
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
                .messageType(com.example.chat.system.domain.enums.MessageType.TEXT)
                .status(com.example.chat.system.domain.enums.MessageStatus.SCHEDULED)
                .createdBy(1L)
                .build();

        ScheduleRule scheduleRule = ScheduleRule.builder()
                .message(message)
                .scheduleType(ScheduleType.ONCE)
                .executionTime(LocalDateTime.now())
                .nextExecutionTime(LocalDateTime.now())
                .isActive(true)
                .jobName("TEST_JOB")
                .jobGroup("TEST_GROUP")
                .executionCount(0)
                .maxExecutionCount(1)
                .build();

        // When
        scheduleRule.markAsExecuted();

        // Then
        assertEquals(1, scheduleRule.getExecutionCount());
        assertFalse(scheduleRule.getIsActive()); // 최대 실행 횟수에 도달하면 비활성화
    }
}