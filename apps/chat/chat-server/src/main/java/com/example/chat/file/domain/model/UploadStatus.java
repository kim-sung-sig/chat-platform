package com.example.chat.file.domain.model;

/** 파일 업로드 상태 */
public enum UploadStatus {
    /** S3 업로드 대기 중 */
    PENDING,
    /** S3 업로드 완료 */
    COMPLETED,
    /** S3 업로드 실패 */
    FAILED
}
