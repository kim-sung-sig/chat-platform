package com.example.chat.file.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("S3Key")
class S3KeyTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("{channelId}/{uuid}/{normalizedFileName} 형식으로 생성된다")
        void happyPath() {
            // when
            S3Key key = S3Key.of("channel-1", "my file.jpg");

            // then
            String[] parts = key.value().split("/");
            assertThat(parts).hasSize(3);
            assertThat(parts[0]).isEqualTo("channel-1");
            assertThat(parts[1]).isNotBlank(); // UUID
            assertThat(parts[2]).isEqualTo("my_file.jpg"); // 공백 → _
        }

        @Test
        @DisplayName("특수문자가 포함된 파일명은 _ 로 치환된다")
        void specialCharactersReplaced() {
            S3Key key = S3Key.of("channel-1", "file name (1).txt");
            String fileName = key.value().split("/")[2];
            assertThat(fileName).doesNotContain(" ", "(", ")");
        }
    }

    @Nested
    @DisplayName("생성자 검증")
    class Constructor {

        @Test
        @DisplayName("null 값 전달 시 IllegalArgumentException이 발생한다")
        void nullValueThrows() {
            assertThatThrownBy(() -> new S3Key(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 전달 시 IllegalArgumentException이 발생한다")
        void blankValueThrows() {
            assertThatThrownBy(() -> new S3Key(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("세그먼트가 2개 미만이면 IllegalArgumentException이 발생한다")
        void insufficientSegmentsThrows() {
            assertThatThrownBy(() -> new S3Key("only-one-segment"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("올바른 형식이면 정상 생성된다")
        void validFormatCreates() {
            assertThatCode(() -> new S3Key("channel-1/uuid-value/file.jpg"))
                    .doesNotThrowAnyException();
        }
    }
}
