package com.example.chat.file.application.service;

import com.example.chat.file.rest.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/** 파일 업로드 Command Port */
public interface FileUploadCommandService {

    /**
     * 파일을 S3에 업로드하고 메타데이터를 저장한다.
     *
     * @param uploaderId 업로더 사용자 ID (JWT에서 추출)
     * @param channelId  업로드 대상 채널 ID
     * @param file       멀티파트 파일
     * @return 업로드 결과 응답
     */
    FileUploadResponse uploadFile(String uploaderId, String channelId, MultipartFile file);
}
