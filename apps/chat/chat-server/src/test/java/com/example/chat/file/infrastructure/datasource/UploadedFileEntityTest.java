package com.example.chat.file.infrastructure.datasource;

import com.example.chat.file.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UploadedFileEntity")
class UploadedFileEntityTest {

    private UploadedFile buildDomain(UploadStatus status) {
        UploadedFile f = new UploadedFile(
                "file-id-1",
                "channel-1",
                "user-1",
                "photo.jpg",
                new S3Key("channel-1/uuid-123/photo.jpg"),
                2048L,
                "image/jpeg",
                FileType.IMAGE,
                ZonedDateTime.parse("2026-03-25T10:00:00+09:00")
        );
        if (status == UploadStatus.COMPLETED) {
            f.markCompleted("https://bucket.s3.amazonaws.com/channel-1/uuid-123/photo.jpg");
        } else if (status == UploadStatus.FAILED) {
            f.markFailed();
        }
        return f;
    }

    @Nested
    @DisplayName("fromDomain()")
    class FromDomain {

        @Test
        @DisplayName("COMPLETED 도메인을 엔티티로 올바르게 변환한다")
        void completedDomainToEntity() {
            // given
            UploadedFile domain = buildDomain(UploadStatus.COMPLETED);

            // when
            UploadedFileEntity entity = UploadedFileEntity.fromDomain(domain);

            // then
            assertThat(entity.getId()).isEqualTo("file-id-1");
            assertThat(entity.getChannelId()).isEqualTo("channel-1");
            assertThat(entity.getUploaderId()).isEqualTo("user-1");
            assertThat(entity.getOriginalFileName()).isEqualTo("photo.jpg");
            assertThat(entity.getS3Key()).isEqualTo("channel-1/uuid-123/photo.jpg");
            assertThat(entity.getFileUrl()).isNotBlank();
            assertThat(entity.getFileSize()).isEqualTo(2048L);
            assertThat(entity.getMimeType()).isEqualTo("image/jpeg");
            assertThat(entity.getFileType()).isEqualTo(FileType.IMAGE);
            assertThat(entity.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        }

        @Test
        @DisplayName("PENDING 도메인은 fileUrl이 null인 엔티티로 변환된다")
        void pendingDomainHasNullFileUrl() {
            UploadedFileEntity entity = UploadedFileEntity.fromDomain(buildDomain(UploadStatus.PENDING));
            assertThat(entity.getFileUrl()).isNull();
            assertThat(entity.getStatus()).isEqualTo(UploadStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomain {

        @Test
        @DisplayName("COMPLETED 엔티티를 도메인으로 올바르게 복원한다")
        void completedEntityToDomain() {
            // given
            UploadedFile source = buildDomain(UploadStatus.COMPLETED);
            UploadedFileEntity entity = UploadedFileEntity.fromDomain(source);

            // when
            UploadedFile domain = entity.toDomain();

            // then
            assertThat(domain.getId()).isEqualTo("file-id-1");
            assertThat(domain.getStatus()).isEqualTo(UploadStatus.COMPLETED);
            assertThat(domain.getFileUrl()).isNotBlank();
        }

        @Test
        @DisplayName("FAILED 엔티티를 도메인으로 복원하면 FAILED 상태이다")
        void failedEntityToDomain() {
            UploadedFile source = buildDomain(UploadStatus.FAILED);
            UploadedFileEntity entity = UploadedFileEntity.fromDomain(source);
            UploadedFile domain = entity.toDomain();

            assertThat(domain.getStatus()).isEqualTo(UploadStatus.FAILED);
        }

        @Test
        @DisplayName("fromDomain → toDomain 왕복 변환 후 핵심 필드가 동일하다")
        void roundTrip() {
            UploadedFile original = buildDomain(UploadStatus.COMPLETED);
            UploadedFile restored = UploadedFileEntity.fromDomain(original).toDomain();

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getChannelId()).isEqualTo(original.getChannelId());
            assertThat(restored.getFileSize()).isEqualTo(original.getFileSize());
            assertThat(restored.getMimeType()).isEqualTo(original.getMimeType());
            assertThat(restored.getFileType()).isEqualTo(original.getFileType());
        }
    }
}
