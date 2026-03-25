package com.example.chat.file.application.service;

import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.file.domain.repository.UploadedFileRepository;
import com.example.chat.file.rest.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

    private final UploadedFileRepository uploadedFileRepository;

    @Override
    @Transactional(readOnly = true)
    public FileUploadResponse getFile(String fileId) {
        return uploadedFileRepository.findById(fileId)
                .map(FileUploadResponse::from)
                .orElseThrow(() -> new ChatException(ChatErrorCode.FILE_NOT_FOUND));
    }
}
