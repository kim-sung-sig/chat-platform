package com.example.chat.approval.rest.controller;

import com.example.chat.approval.application.service.ApprovalCommandService;
import com.example.chat.approval.application.service.ApprovalQueryService;
import com.example.chat.approval.rest.dto.request.ApprovalCreateRequest;
import com.example.chat.approval.rest.dto.response.ApprovalInboxResponse;
import com.example.chat.approval.rest.dto.response.ApprovalResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {
    private final ApprovalCommandService commandService;
    private final ApprovalQueryService queryService;

    public ApprovalController(ApprovalCommandService commandService, ApprovalQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ApprovalResponse> create(@Valid @RequestBody ApprovalCreateRequest request) {
        throw new UnsupportedOperationException("TODO: map request to domain and return response");
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO: implement submit endpoint");
    }

    @PostMapping("/{id}/lines/{line}/approve")
    public ResponseEntity<Void> approve(@PathVariable String id, @PathVariable int line) {
        throw new UnsupportedOperationException("TODO: implement approve endpoint");
    }

    @PostMapping("/{id}/lines/{line}/reject")
    public ResponseEntity<Void> reject(@PathVariable String id, @PathVariable int line) {
        throw new UnsupportedOperationException("TODO: implement reject endpoint");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO: implement cancel endpoint");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponse> getById(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO: implement get document status");
    }

    @GetMapping("/inbox")
    public ResponseEntity<ApprovalInboxResponse> inbox(@RequestParam String approverId) {
        throw new UnsupportedOperationException("TODO: implement inbox query");
    }
}
