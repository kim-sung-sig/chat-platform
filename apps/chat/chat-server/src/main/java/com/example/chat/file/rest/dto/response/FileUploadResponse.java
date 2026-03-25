package com.example.chat.file.rest.dto.response;

import com.example.chat.file.domain.model.FileType;
import com.example.chat.file.domain.model.UploadedFile;

import java.time.ZonedDateTime;

/**
 * 파일 업로드/조회 응답 DTO
 */
public record FileUploadResponse(
        String fileId,
        String fileUrl,
        String fileName,
        long fileSize,
        String mimeType,
        FileType fileType,
        ZonedDateTime uploadedAt
) {
    public static FileUploadResponse from(UploadedFile domain) {
        return new FileUploadResponse(
                domain.getId(),
                domain.getFileUrl(),
                domain.getOriginalFileName(),
                domain.getFileSize(),
                domain.getMimeType(),
                domain.getFileType(),
                domain.getUploadedAt()
        );
    }
}
