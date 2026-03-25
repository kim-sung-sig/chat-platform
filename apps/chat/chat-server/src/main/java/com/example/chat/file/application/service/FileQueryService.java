package com.example.chat.file.application.service;

import com.example.chat.file.rest.dto.response.FileUploadResponse;

/** 파일 Query Port */
public interface FileQueryService {

    /**
     * 파일 메타데이터를 조회한다.
     *
     * @param fileId 파일 ID
     * @return 파일 응답 DTO
     */
    FileUploadResponse getFile(String fileId);
}
