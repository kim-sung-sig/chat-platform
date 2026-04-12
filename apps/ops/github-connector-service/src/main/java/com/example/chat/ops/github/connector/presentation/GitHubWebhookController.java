package com.example.chat.ops.github.connector.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.contract.error.OpsErrorCode;
import com.example.chat.ops.contract.error.OpsException;
import com.example.chat.ops.contract.event.EventEnvelope;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import com.example.chat.ops.contract.ticket.TicketRef;
import com.example.chat.ops.github.connector.application.GitHubTicketSyncService;
import com.example.chat.ops.github.connector.application.GitHubWebhookSignatureVerifier;
import com.example.chat.ops.github.connector.presentation.dto.GitHubTicketSyncRequest;
import com.example.chat.ops.github.connector.presentation.dto.GitHubWebhookAckResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class GitHubWebhookController {
    private final GitHubWebhookSignatureVerifier signatureVerifier;
    private final GitHubTicketSyncService ticketSyncService;

    @Value("${ops.github.webhook-secret:change-me}")
    private String webhookSecret;

    public GitHubWebhookController(
            GitHubWebhookSignatureVerifier signatureVerifier,
            GitHubTicketSyncService ticketSyncService
    ) {
        this.signatureVerifier = signatureVerifier;
        this.ticketSyncService = ticketSyncService;
    }

    @PostMapping("/github/webhook")
    public ResponseEntity<ApiResponse<GitHubWebhookAckResponse>> receiveWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String eventType,
            @RequestHeader(value = "X-GitHub-Delivery", defaultValue = "N/A") String deliveryId,
            @RequestBody String payload
    ) {
        boolean valid = signatureVerifier.isValid(payload, signature, webhookSecret);
        if (!valid) {
            throw new OpsException(OpsErrorCode.GITHUB_SIGNATURE_INVALID);
        }

        return ApiResponse.ok(new GitHubWebhookAckResponse(deliveryId, eventType, true)).toResponseEntity();
    }

    @PostMapping("/tickets/sync")
    public ResponseEntity<ApiResponse<EventEnvelope<TicketRef>>> syncTicket(
            @RequestHeader("X-Project-Role") String role,
            @Valid @RequestBody GitHubTicketSyncRequest request
    ) {
        OpsAuthorization.require(role, OpsAction.TICKET_SYNC);
        EventEnvelope<TicketRef> event = ticketSyncService.sync(request);
        return ApiResponse.ok(event).toResponseEntity();
    }
}
