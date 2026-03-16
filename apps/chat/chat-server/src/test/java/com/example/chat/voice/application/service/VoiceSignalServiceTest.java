package com.example.chat.voice.application.service;

import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.storage.domain.entity.ChatChannelEntity;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import com.example.chat.storage.domain.repository.JpaChannelRepository;
import com.example.chat.voice.domain.model.VoiceSignal;
import com.example.chat.voice.domain.model.VoiceSignalType;
import com.example.chat.voice.domain.repository.VoiceSignalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceSignalService")
class VoiceSignalServiceTest {

    @Mock
    VoiceSignalRepository signalRepository;

    @Mock
    JpaChannelRepository channelRepository;

    @Mock
    JpaChannelMemberRepository channelMemberRepository;

    @InjectMocks
    VoiceSignalService service;

    @Nested
    @DisplayName("send")
    class Send {

        @Nested
        @DisplayName("성공")
        class HappyPath {
            @Test
            @DisplayName("멤버는 시그널을 전송할 수 있다")
            void givenMembers_whenSend_thenSaved() {
                // Given
                ChatChannelEntity channel = activeChannel("ch-1");
                when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "from")).thenReturn(true);
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "to")).thenReturn(true);
                when(signalRepository.save(any(VoiceSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                VoiceSignal signal = service.send("ch-1", "from", "to", VoiceSignalType.OFFER, "payload");

                // Then
                assertEquals("from", signal.getFromUserId());
                assertEquals("to", signal.getToUserId());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failure {
            @Test
            @DisplayName("채팅방 멤버가 아니면 예외가 발생한다")
            void givenNotMember_whenSend_thenThrows() {
                // Given
                ChatChannelEntity channel = activeChannel("ch-1");
                when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "from")).thenReturn(false);

                // When / Then
                assertThrows(ChatException.class, () -> service.send("ch-1", "from", "to", VoiceSignalType.OFFER, "payload"));
            }
        }
    }

    @Nested
    @DisplayName("pull")
    class Pull {

        @Nested
        @DisplayName("성공")
        class HappyPath {
            @Test
            @DisplayName("수신자는 자신의 시그널을 조회하고 삭제한다")
            void givenSignals_whenPull_thenReturnAndDelete() {
                // Given
                ChatChannelEntity channel = activeChannel("ch-1");
                when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
                when(channelMemberRepository.existsByChannelIdAndUserId("ch-1", "user-1")).thenReturn(true);
                when(signalRepository.findByChannelIdAndToUserId("ch-1", "user-1")).thenReturn(List.of());

                // When
                service.pull("ch-1", "user-1");

                // Then
                verify(signalRepository).deleteAll("ch-1", "user-1");
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
