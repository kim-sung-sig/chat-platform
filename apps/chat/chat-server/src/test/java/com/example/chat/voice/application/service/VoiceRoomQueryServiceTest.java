package com.example.chat.voice.application.service;

import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.voice.domain.model.VoiceRoom;
import com.example.chat.voice.domain.repository.VoiceRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceRoomQueryService")
class VoiceRoomQueryServiceTest {

    @Mock
    VoiceRoomRepository voiceRoomRepository;

    @Mock
    JpaChannelRepository channelRepository;

    @Mock
    JpaChannelMemberRepository channelMemberRepository;

    @InjectMocks
    VoiceRoomQueryService service;

    @Nested
    @DisplayName("listParticipants")
    class ListParticipants {

        @Nested
        @DisplayName("성공")
        class HappyPath {
            @Test
            @DisplayName("멤버는 참여자 목록을 조회할 수 있다")
            void givenMember_whenList_thenReturnParticipants() {
                // Given
                ChatChannelEntity channel = activeChannel("ch-1");
                when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "user-1")).thenReturn(true);

                VoiceRoom room = new VoiceRoom("ch-1", Instant.now());
                room.addParticipant("user-1", Instant.now());
                when(voiceRoomRepository.findByChannelId("ch-1")).thenReturn(Optional.of(room));

                // When
                var participants = service.listParticipants("ch-1", "user-1");

                // Then
                assertEquals(1, participants.size());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failure {
            @Test
            @DisplayName("채팅방 멤버가 아니면 예외가 발생한다")
            void givenNotMember_whenList_thenThrows() {
                // Given
                ChatChannelEntity channel = activeChannel("ch-1");
                when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "user-1")).thenReturn(false);

                // When / Then
                assertThrows(ChatException.class, () -> service.listParticipants("ch-1", "user-1"));
            }
        }
    }

    private ChatChannelEntity activeChannel(String id) {
        return ChatChannelEntity.builder()
                .id(id)
                .name("channel")
                .description("desc")
                .channelType(ChannelType.PUBLIC)
                .ownerId("owner-1")
                .active(true)
                .build();
    }
}
