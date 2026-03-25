package com.example.chat.scheduled.infrastructure.datasource;

import com.example.chat.message.domain.MessageContent;
import com.example.chat.scheduled.domain.model.ScheduleStatus;
import com.example.chat.scheduled.domain.model.ScheduleType;
import com.example.chat.scheduled.domain.model.ScheduledMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [단위 테스트] ScheduledMessageEntity 매핑
 *
 * 검증 범위:
 * - fromDomain() → toDomain() 라운드트립 (TEXT / IMAGE / FILE)
 * - ZonedDateTime UTC 변환 정확성
 */
@DisplayName("ScheduledMessageEntity 매핑 단위 테스트")
class ScheduledMessageEntityTest {

    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.of("UTC"));

    // ── Fixture ───────────────────────────────────────────────────────

    private ScheduledMessage textDomain() {
        return new ScheduledMessage(
                "id-001", "ch-001", "user-001",
                MessageContent.text("안녕하세요"),
                ScheduleType.ONCE, ScheduleStatus.PENDING,
                NOW.plusHours(1), NOW, null, null, 0
        );
    }

    private ScheduledMessage imageDomain() {
        return new ScheduledMessage(
                "id-002", "ch-001", "user-001",
                MessageContent.image("https://cdn/img.png", "img.png", 1024L),
                ScheduleType.ONCE, ScheduleStatus.PENDING,
                NOW.plusHours(1), NOW, null, null, 0
        );
    }

    private ScheduledMessage fileDomain() {
        return new ScheduledMessage(
                "id-003", "ch-001", "user-001",
                MessageContent.file("https://cdn/doc.pdf", "doc.pdf", 2048L, "application/pdf"),
                ScheduleType.ONCE, ScheduleStatus.PENDING,
                NOW.plusHours(1), NOW, null, null, 0
        );
    }

    // ── fromDomain → toDomain 라운드트립 ─────────────────────────────

    @Nested
    @DisplayName("TEXT 메시지 매핑")
    class TextContent {

        @Test
        @DisplayName("TEXT 도메인 → Entity → 도메인 라운드트립 — 모든 필드 일치")
        void roundTrip_text() {
            // Given
            ScheduledMessage original = textDomain();

            // When
            ScheduledMessageEntity entity = ScheduledMessageEntity.fromDomain(original);
            ScheduledMessage restored = entity.toDomain();

            // Then
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getChannelId()).isEqualTo(original.getChannelId());
            assertThat(restored.getSenderId()).isEqualTo(original.getSenderId());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getRetryCount()).isEqualTo(original.getRetryCount());
            assertThat(restored.getContent()).isInstanceOf(MessageContent.Text.class);
            assertThat(restored.getContent().getText()).isEqualTo("안녕하세요");
        }
    }

    @Nested
    @DisplayName("IMAGE 메시지 매핑")
    class ImageContent {

        @Test
        @DisplayName("IMAGE 도메인 → Entity → 도메인 라운드트립 — mediaUrl/fileName/fileSize 일치")
        void roundTrip_image() {
            // Given
            ScheduledMessage original = imageDomain();

            // When
            ScheduledMessageEntity entity = ScheduledMessageEntity.fromDomain(original);
            ScheduledMessage restored = entity.toDomain();

            // Then
            assertThat(restored.getContent()).isInstanceOf(MessageContent.Image.class);
            assertThat(restored.getContent().getMediaUrl()).isEqualTo("https://cdn/img.png");
            assertThat(restored.getContent().getFileName()).isEqualTo("img.png");
            assertThat(restored.getContent().getFileSize()).isEqualTo(1024L);
        }
    }

    @Nested
    @DisplayName("FILE 메시지 매핑")
    class FileContent {

        @Test
        @DisplayName("FILE 도메인 → Entity → 도메인 라운드트립 — mimeType 포함 모든 필드 일치")
        void roundTrip_file() {
            // Given
            ScheduledMessage original = fileDomain();

            // When
            ScheduledMessageEntity entity = ScheduledMessageEntity.fromDomain(original);
            ScheduledMessage restored = entity.toDomain();

            // Then
            assertThat(restored.getContent()).isInstanceOf(MessageContent.File.class);
            assertThat(restored.getContent().getMediaUrl()).isEqualTo("https://cdn/doc.pdf");
            assertThat(restored.getContent().getMimeType()).isEqualTo("application/pdf");
        }
    }

    // ── ZonedDateTime 변환 ────────────────────────────────────────────

    @Nested
    @DisplayName("ZonedDateTime UTC 변환")
    class DateTimeConversion {

        @Test
        @DisplayName("KST(+09:00) 시각 → Entity 저장 → 복원 시 UTC 동일 instant")
        void givenKstDateTime_whenRoundTrip_thenSameInstant() {
            // Given
            ZonedDateTime kst = ZonedDateTime.parse("2026-03-26T09:00:00+09:00");
            ScheduledMessage original = new ScheduledMessage(
                    "id-kst", "ch-001", "user-001",
                    MessageContent.text("KST 예약"),
                    ScheduleType.ONCE, ScheduleStatus.PENDING,
                    kst, NOW, null, null, 0
            );

            // When
            ScheduledMessageEntity entity = ScheduledMessageEntity.fromDomain(original);
            ScheduledMessage restored = entity.toDomain();

            // Then: 동일 Instant (toEpochSecond 비교)
            assertThat(restored.getScheduledAt().toEpochSecond())
                    .isEqualTo(original.getScheduledAt().toEpochSecond());
        }

        @Test
        @DisplayName("executedAt / cancelledAt null 허용 - NullPointerException 없음")
        void givenNullOptionalDates_whenRoundTrip_thenNoNpe() {
            // Given
            ScheduledMessage original = textDomain(); // executedAt, cancelledAt = null

            // When / Then: 예외 없이 완료
            ScheduledMessageEntity entity = ScheduledMessageEntity.fromDomain(original);
            ScheduledMessage restored = entity.toDomain();

            assertThat(restored.getExecutedAt()).isNull();
            assertThat(restored.getCancelledAt()).isNull();
        }
    }
}