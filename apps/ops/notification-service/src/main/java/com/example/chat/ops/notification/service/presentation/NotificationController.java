package com.example.chat.ops.notification.service.presentation;

import com.example.chat.common.web.response.ApiResponse;
import com.example.chat.ops.contract.event.EventEnvelope;
import com.example.chat.ops.contract.rbac.OpsAction;
import com.example.chat.ops.contract.rbac.OpsAuthorization;
import com.example.chat.ops.notification.service.application.NotificationService;
import com.example.chat.ops.notification.service.presentation.dto.CreateNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventEnvelope<CreateNotificationRequest>>> publish(
            @RequestHeader("X-Project-Role") String role,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        OpsAuthorization.require(role, OpsAction.NOTIFICATION_PUBLISH);
        return ApiResponse.ok(notificationService.publish(request)).toResponseEntity();
    }
}
