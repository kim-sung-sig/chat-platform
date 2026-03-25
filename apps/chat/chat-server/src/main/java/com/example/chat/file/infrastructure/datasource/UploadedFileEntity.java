package com.example.chat.file.infrastructure.datasource;

import com.example.chat.file.domain.model.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * uploaded_files 테이블 JPA Entity
 *
 * 도메인 객체(UploadedFile)와 분리된 영속성 모델.
 * toDomain() / fromDomain() 으로 변환한다.
 */
@Entity
@Table(name = "uploaded_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFileEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "channel_id", length = 36, nullable = false)
    private String channelId;

    @Column(name = "uploader_id", length = 36, nullable = false)
    private String uploaderId;

    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;

    @Column(name = "s3_key", length = 500, nullable = false, unique = true)
    private String s3Key;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20, nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UploadStatus status;

    @Column(name = "uploaded_at", nullable = false)
    private java.time.LocalDateTime uploadedAt;

    // ── 변환 ─────────────────────────────────────────────────────────────────

    public UploadedFile toDomain() {
        UploadedFile domain = new UploadedFile(
                id,
                channelId,
                uploaderId,
                originalFileName,
                new S3Key(s3Key),
                fileSize,
                mimeType,
                fileType,
                toZoned(uploadedAt)
        );
        if (status == UploadStatus.COMPLETED) {
            domain.markCompleted(fileUrl);
        } else if (status == UploadStatus.FAILED) {
            domain.markFailed();
        }
        return domain;
    }

    public static UploadedFileEntity fromDomain(UploadedFile domain) {
        UploadedFileEntity e = new UploadedFileEntity();
        e.id             = domain.getId();
        e.channelId      = domain.getChannelId();
        e.uploaderId     = domain.getUploaderId();
        e.originalFileName = domain.getOriginalFileName();
        e.s3Key          = domain.getS3Key().value();
        e.fileUrl        = domain.getFileUrl();
        e.fileSize       = domain.getFileSize();
        e.mimeType       = domain.getMimeType();
        e.fileType       = domain.getFileType();
        e.status         = domain.getStatus();
        e.uploadedAt     = toLocal(domain.getUploadedAt());
        return e;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static ZonedDateTime toZoned(java.time.LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneId.of("UTC"));
    }

    private static java.time.LocalDateTime toLocal(ZonedDateTime zdt) {
        return zdt == null ? null : zdt.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }
}
