package com.example.chat.file.rest.controller;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.file.application.service.FileQueryService;
import com.example.chat.file.application.service.FileUploadCommandService;
import com.example.chat.file.rest.dto.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadCommandService fileUploadCommandService;
    private final FileQueryService fileQueryService;

    /**
     * POST /api/files/upload
     * multipart/form-data 파일 업로드
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FileUploadResponse uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("channelId") String channelId) {

        String uploaderId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return fileUploadCommandService.uploadFile(uploaderId, channelId, file);
    }

    /**
     * GET /api/files/{fileId}
     * 파일 메타데이터 조회
     */
    @GetMapping("/{fileId}")
    public FileUploadResponse getFile(@PathVariable String fileId) {
        return fileQueryService.getFile(fileId);
    }
}
