package com.example.chat.file.domain.service;

import com.example.chat.file.domain.model.S3Key;

/** 외부 스토리지 Port (domain layer — Spring 미의존) */
public interface S3StorageService {

    /**
     * S3에 바이트 배열 업로드.
     *
     * @param s3Key    업로드할 S3 키
     * @param bytes    파일 바이트
     * @param mimeType Content-Type
     * @return 퍼블릭 접근 URL
     */
    String upload(S3Key s3Key, byte[] bytes, String mimeType);
}
