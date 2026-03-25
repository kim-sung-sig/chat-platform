package com.example.chat.file.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UploadedFile")
class UploadedFileTest {

    private UploadedFile sut;

    @BeforeEach
    void setUp() {
        sut = new UploadedFile(
                "file-id-1",
                "channel-1",
                "user-1",
                "test.jpg",
                S3Key.of("channel-1", "test.jpg"),
                1024L,
                "image/jpeg",
                FileType.IMAGE,
                ZonedDateTime.now()
        );
    }

    @Nested
    @DisplayName("markCompleted()")
    class MarkCompleted {

        @Test
        @DisplayName("정상 URL 전달 시 상태가 COMPLETED로 전이된다")
        void happyPath() {
            // when
            sut.markCompleted("https://bucket.s3.ap-northeast-2.amazonaws.com/channel-1/uuid/test.jpg");

            // then
            assertThat(sut.getStatus()).isEqualTo(UploadStatus.COMPLETED);
            assertThat(sut.getFileUrl()).isEqualTo(
                    "https://bucket.s3.ap-northeast-2.amazonaws.com/channel-1/uuid/test.jpg");
        }

        @Test
        @DisplayName("최초 상태는 PENDING이다")
        void initialStatusIsPending() {
            assertThat(sut.getStatus()).isEqualTo(UploadStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("markFailed()")
    class MarkFailed {

        @Test
        @DisplayName("호출 시 상태가 FAILED로 전이된다")
        void happyPath() {
            // when
            sut.markFailed();

            // then
            assertThat(sut.getStatus()).isEqualTo(UploadStatus.FAILED);
        }

        @Test
        @DisplayName("FAILED 상태에서 fileUrl은 null이다")
        void fileUrlIsNullWhenFailed() {
            sut.markFailed();
            assertThat(sut.getFileUrl()).isNull();
        }
    }
}
