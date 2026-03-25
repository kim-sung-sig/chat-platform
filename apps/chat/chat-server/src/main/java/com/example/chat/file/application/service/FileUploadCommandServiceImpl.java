package com.example.chat.file.application.service;

import com.example.chat.shared.exception.ChatException;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.file.domain.model.*;
import com.example.chat.file.domain.repository.UploadedFileRepository;
import com.example.chat.file.domain.service.S3StorageService;
import com.example.chat.file.rest.dto.response.FileUploadResponse;
import com.example.chat.storage.domain.repository.JpaChannelMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadCommandServiceImpl implements FileUploadCommandService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;  // 10 MB
    private static final long MAX_OTHER_SIZE = 50L * 1024 * 1024;  // 50 MB

    private final UploadedFileRepository uploadedFileRepository;
    private final S3StorageService s3StorageService;
    private final JpaChannelMemberRepository channelMemberRepository;

    @Override
    @Transactional
    public FileUploadResponse uploadFile(String uploaderId, String channelId, MultipartFile file) {
        validateFile(uploaderId, channelId, file);

        FileType fileType = FileType.from(file.getContentType());
        S3Key s3Key = S3Key.of(channelId, file.getOriginalFilename());

        UploadedFile uploadedFile = new UploadedFile(
                UUID.randomUUID().toString(),
                channelId,
                uploaderId,
                file.getOriginalFilename(),
                s3Key,
                file.getSize(),
                file.getContentType(),
                fileType,
                ZonedDateTime.now()
        );

        uploadedFileRepository.save(uploadedFile);
        log.info("파일 업로드 시작: fileId={}, channelId={}, fileName={}, size={}",
                uploadedFile.getId(), channelId, file.getOriginalFilename(), file.getSize());

        try {
            String fileUrl = s3StorageService.upload(s3Key, file.getBytes(), file.getContentType());
            uploadedFile.markCompleted(fileUrl);
            log.info("파일 업로드 완료: fileId={}, fileUrl={}", uploadedFile.getId(), fileUrl);
        } catch (Exception e) {
            uploadedFile.markFailed();
            uploadedFileRepository.save(uploadedFile);
            log.error("파일 업로드 실패: fileId={}, error={}", uploadedFile.getId(), e.getMessage());
            throw new ChatException(ChatErrorCode.FILE_UPLOAD_FAILED);
        }

        return FileUploadResponse.from(uploadedFileRepository.save(uploadedFile));
    }

    private void validateFile(String uploaderId, String channelId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ChatException(ChatErrorCode.FILE_EMPTY);
        }

        FileType fileType = FileType.from(file.getContentType());
        if (!fileType.isAllowed()) {
            throw new ChatException(ChatErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        long maxSize = fileType.isImage() ? MAX_IMAGE_SIZE : MAX_OTHER_SIZE;
        if (file.getSize() > maxSize) {
            throw new ChatException(ChatErrorCode.FILE_SIZE_EXCEEDED);
        }

        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, uploaderId)) {
            throw new ChatException(ChatErrorCode.CHANNEL_NOT_MEMBER);
        }
    }
}
