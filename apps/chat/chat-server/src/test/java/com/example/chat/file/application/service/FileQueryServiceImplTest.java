package com.example.chat.file.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.file.domain.model.*;
import com.example.chat.file.domain.repository.UploadedFileRepository;
import com.example.chat.file.rest.dto.response.FileUploadResponse;
import com.example.chat.shared.exception.ChatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileQueryServiceImpl")
class FileQueryServiceImplTest {

    @Mock
    UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    FileQueryServiceImpl sut;

    private UploadedFile completedFile() {
        UploadedFile f = new UploadedFile(
                "file-id-1",
                "channel-1",
                "user-1",
                "test.jpg",
                new S3Key("channel-1/uuid-123/test.jpg"),
                1024L,
                "image/jpeg",
                FileType.IMAGE,
                ZonedDateTime.now()
        );
        f.markCompleted("https://bucket.s3.amazonaws.com/channel-1/uuid-123/test.jpg");
        return f;
    }

    @Nested
    @DisplayName("getFile()")
    class GetFile {

        @Test
        @DisplayName("존재하는 fileId 조회 시 파일 응답을 반환한다")
        void happyPath() {
            // given
            given(uploadedFileRepository.findById("file-id-1"))
                    .willReturn(Optional.of(completedFile()));

            // when
            FileUploadResponse response = sut.getFile("file-id-1");

            // then
            assertThat(response.fileId()).isEqualTo("file-id-1");
            assertThat(response.fileUrl()).isNotBlank();
            assertThat(response.fileType()).isEqualTo(FileType.IMAGE);
        }

        @Test
        @DisplayName("존재하지 않는 fileId 조회 시 FILE_NOT_FOUND 예외가 발생한다")
        void notFound() {
            // given
            given(uploadedFileRepository.findById("no-such-id"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getFile("no-such-id"))
                    .isInstanceOf(ChatException.class)
                    .extracting(ex -> ((ChatException) ex).getErrorCode())
                    .isEqualTo(ChatErrorCode.FILE_NOT_FOUND);
        }
    }
}
