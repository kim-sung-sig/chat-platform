package com.example.chat.file.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileType")
class FileTypeTest {

    @Nested
    @DisplayName("from()")
    class From {

        @ParameterizedTest
        @ValueSource(strings = {"image/jpeg", "image/png", "image/gif", "image/webp"})
        @DisplayName("이미지 MIME 타입은 IMAGE로 분류된다")
        void imageMimeTypes(String mime) {
            assertThat(FileType.from(mime)).isEqualTo(FileType.IMAGE);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        })
        @DisplayName("문서 MIME 타입은 DOCUMENT로 분류된다")
        void documentMimeTypes(String mime) {
            assertThat(FileType.from(mime)).isEqualTo(FileType.DOCUMENT);
        }

        @ParameterizedTest
        @ValueSource(strings = {"audio/mpeg", "audio/aac"})
        @DisplayName("오디오 MIME 타입은 AUDIO로 분류된다")
        void audioMimeTypes(String mime) {
            assertThat(FileType.from(mime)).isEqualTo(FileType.AUDIO);
        }

        @ParameterizedTest
        @ValueSource(strings = {"video/mp4", "video/webm"})
        @DisplayName("비디오 MIME 타입은 VIDEO로 분류된다")
        void videoMimeTypes(String mime) {
            assertThat(FileType.from(mime)).isEqualTo(FileType.VIDEO);
        }

        @Test
        @DisplayName("알 수 없는 MIME 타입은 OTHER로 분류된다")
        void unknownMimeReturnsOther() {
            assertThat(FileType.from("application/x-unknown")).isEqualTo(FileType.OTHER);
        }

        @Test
        @DisplayName("null MIME 타입은 OTHER로 분류된다")
        void nullMimeReturnsOther() {
            assertThat(FileType.from(null)).isEqualTo(FileType.OTHER);
        }

        @Test
        @DisplayName("대문자 MIME 타입도 정상 분류된다")
        void caseInsensitive() {
            assertThat(FileType.from("IMAGE/JPEG")).isEqualTo(FileType.IMAGE);
        }
    }

    @Nested
    @DisplayName("isAllowed()")
    class IsAllowed {

        @Test
        @DisplayName("OTHER는 업로드 불허")
        void otherIsNotAllowed() {
            assertThat(FileType.OTHER.isAllowed()).isFalse();
        }

        @Test
        @DisplayName("IMAGE는 업로드 허용")
        void imageIsAllowed() {
            assertThat(FileType.IMAGE.isAllowed()).isTrue();
        }
    }
}
