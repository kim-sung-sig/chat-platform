package com.example.chat.file.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.file.domain.model.*;
import com.example.chat.file.domain.repository.UploadedFileRepository;
import com.example.chat.file.domain.service.S3StorageService;
import com.example.chat.file.rest.dto.response.FileUploadResponse;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadCommandServiceImpl")
class FileUploadCommandServiceImplTest {

    @Mock
    UploadedFileRepository uploadedFileRepository;

    @Mock
    S3StorageService s3StorageService;

    @Mock
    JpaChannelMemberRepository channelMemberRepository;

    @InjectMocks
    FileUploadCommandServiceImpl sut;

    private static final String UPLOADER_ID = "user-1";
    private static final String CHANNEL_ID  = "channel-1";
    private static final String FILE_URL    = "https://bucket.s3.amazonaws.com/channel-1/uuid/test.jpg";

    private MockMultipartFile imageFile(long sizeBytes) {
        return new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[(int) sizeBytes]);
    }

    private MockMultipartFile otherFile(long sizeBytes, String mimeType) {
        return new MockMultipartFile(
                "file", "test.pdf", mimeType, new byte[(int) sizeBytes]);
    }

    @Nested
    @DisplayName("uploadFile()")
    class UploadFile {

        @Nested
        @DisplayName("정상 업로드")
        class HappyPath {

            @Test
            @DisplayName("이미지 5MB 업로드 시 COMPLETED 응답을 반환하고 S3 업로드가 호출된다")
            void uploadImage5MB() {
                // given
                long fiveMB = 5L * 1024 * 1024;
                MockMultipartFile file = imageFile(fiveMB);
                given(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, UPLOADER_ID))
                        .willReturn(true);
                given(s3StorageService.upload(any(S3Key.class), any(byte[].class), anyString()))
                        .willReturn(FILE_URL);

                ArgumentCaptor<UploadedFile> captor = ArgumentCaptor.forClass(UploadedFile.class);
                given(uploadedFileRepository.save(captor.capture()))
                        .willAnswer(inv -> captor.getValue());

                // when
                FileUploadResponse response = sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file);

                // then
                then(s3StorageService).should().upload(any(S3Key.class), any(byte[].class), eq("image/jpeg"));
                assertThat(response.fileUrl()).isEqualTo(FILE_URL);
                assertThat(response.fileType()).isEqualTo(FileType.IMAGE);
            }
        }

        @Nested
        @DisplayName("파일 크기 초과")
        class FileSizeExceeded {

            @Test
            @DisplayName("이미지 11MB 초과 시 FILE_SIZE_EXCEEDED 예외가 발생한다")
            void image11MB() {
                // given - 채널 멤버 검증 전에 파일 크기 검증이 먼저 실행된다
                long elevenMB = 11L * 1024 * 1024;
                MockMultipartFile file = imageFile(elevenMB);

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_SIZE_EXCEEDED);
            }

            @Test
            @DisplayName("기타 파일 51MB 초과 시 FILE_SIZE_EXCEEDED 예외가 발생한다")
            void other51MB() {
                // given
                long fiftyOneMB = 51L * 1024 * 1024;
                MockMultipartFile file = otherFile(fiftyOneMB, "application/pdf");

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_SIZE_EXCEEDED);
            }
        }

        @Nested
        @DisplayName("파일 타입 불허")
        class FileTypeNotAllowed {

            @Test
            @DisplayName("허용되지 않는 MIME 타입이면 FILE_TYPE_NOT_ALLOWED 예외가 발생한다")
            void unknownMime() {
                // given - 채널 멤버 검증 전에 파일 타입 검증이 먼저 실행된다
                MockMultipartFile file = new MockMultipartFile(
                        "file", "test.exe", "application/x-msdownload", new byte[1024]);

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_TYPE_NOT_ALLOWED);
            }
        }

        @Nested
        @DisplayName("빈 파일")
        class EmptyFile {

            @Test
            @DisplayName("isEmpty()인 파일이면 FILE_EMPTY 예외가 발생한다")
            void emptyFile() {
                // given - 채널 멤버 검증 전에 빈 파일 검증이 먼저 실행된다
                MockMultipartFile file = new MockMultipartFile(
                        "file", "empty.jpg", "image/jpeg", new byte[0]);

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_EMPTY);
            }

            @Test
            @DisplayName("null 파일이면 FILE_EMPTY 예외가 발생한다")
            void nullFile() {
                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, null))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_EMPTY);
            }
        }

        @Nested
        @DisplayName("채널 미가입")
        class ChannelNotMember {

            @Test
            @DisplayName("채널 미가입자면 CHANNEL_NOT_MEMBER 예외가 발생한다")
            void notMember() {
                // given
                MockMultipartFile file = imageFile(1024);
                given(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, UPLOADER_ID))
                        .willReturn(false);

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.CHANNEL_NOT_MEMBER);
            }
        }

        @Nested
        @DisplayName("S3 업로드 실패")
        class S3UploadFailed {

            @Test
            @DisplayName("S3 업로드 실패 시 FAILED 상태 저장 후 FILE_UPLOAD_FAILED 예외가 발생한다")
            void s3Fails() {
                // given
                MockMultipartFile file = imageFile(1024);
                given(channelMemberRepository.existsByChannelIdAndUserId(CHANNEL_ID, UPLOADER_ID))
                        .willReturn(true);
                given(s3StorageService.upload(any(), any(), any()))
                        .willThrow(new RuntimeException("S3 error"));

                ArgumentCaptor<UploadedFile> captor = ArgumentCaptor.forClass(UploadedFile.class);
                given(uploadedFileRepository.save(captor.capture()))
                        .willAnswer(inv -> captor.getValue());

                // when & then
                assertThatThrownBy(() -> sut.uploadFile(UPLOADER_ID, CHANNEL_ID, file))
                        .isInstanceOf(ChatException.class)
                        .extracting(ex -> ((ChatException) ex).getErrorCode())
                        .isEqualTo(ChatErrorCode.FILE_UPLOAD_FAILED);

                // FAILED 상태로 저장되었는지 검증
                assertThat(captor.getAllValues())
                        .anySatisfy(saved -> assertThat(saved.getStatus()).isEqualTo(UploadStatus.FAILED));
            }
        }
    }
}
