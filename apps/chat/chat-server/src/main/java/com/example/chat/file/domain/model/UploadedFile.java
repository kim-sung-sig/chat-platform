package com.example.chat.file.domain.model;

import java.time.ZonedDateTime;

/**
 * 업로드 파일 Aggregate Root (순수 POJO — Spring/JPA 의존 금지)
 */
public class UploadedFile {

    private final String id;
    private final String channelId;
    private final String uploaderId;
    private final String originalFileName;
    private final S3Key s3Key;
    private String fileUrl;
    private final long fileSize;
    private final String mimeType;
    private final FileType fileType;
    private UploadStatus status;
    private final ZonedDateTime uploadedAt;

    public UploadedFile(String id,
                        String channelId,
                        String uploaderId,
                        String originalFileName,
                        S3Key s3Key,
                        long fileSize,
                        String mimeType,
                        FileType fileType,
                        ZonedDateTime uploadedAt) {
        this.id = id;
        this.channelId = channelId;
        this.uploaderId = uploaderId;
        this.originalFileName = originalFileName;
        this.s3Key = s3Key;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileType = fileType;
        this.status = UploadStatus.PENDING;
        this.uploadedAt = uploadedAt;
    }

    /** S3 업로드 완료 상태로 전이 */
    public void markCompleted(String fileUrl) {
        this.fileUrl = fileUrl;
        this.status = UploadStatus.COMPLETED;
    }

    /** S3 업로드 실패 상태로 전이 */
    public void markFailed() {
        this.status = UploadStatus.FAILED;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()               { return id; }
    public String getChannelId()        { return channelId; }
    public String getUploaderId()       { return uploaderId; }
    public String getOriginalFileName() { return originalFileName; }
    public S3Key  getS3Key()            { return s3Key; }
    public String getFileUrl()          { return fileUrl; }
    public long   getFileSize()         { return fileSize; }
    public String getMimeType()         { return mimeType; }
    public FileType  getFileType()      { return fileType; }
    public UploadStatus getStatus()     { return status; }
    public ZonedDateTime getUploadedAt(){ return uploadedAt; }
}
