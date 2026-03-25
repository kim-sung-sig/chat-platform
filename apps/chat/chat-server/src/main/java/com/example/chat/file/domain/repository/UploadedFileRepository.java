package com.example.chat.file.domain.repository;

import com.example.chat.file.domain.model.UploadedFile;

import java.util.Optional;

/** 업로드 파일 Repository Port (domain layer) */
public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(String id);
}
