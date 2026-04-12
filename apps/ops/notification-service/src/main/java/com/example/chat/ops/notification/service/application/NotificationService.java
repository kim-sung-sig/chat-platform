package com.example.chat.ops.notification.service.application;

import com.example.chat.ops.contract.event.EventEnvelope;
import com.example.chat.ops.notification.service.presentation.dto.CreateNotificationRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public EventEnvelope<CreateNotificationRequest> publish(CreateNotificationRequest request) {
        return EventEnvelope.of(UUID.randomUUID().toString(), request.projectId(), "notification.published", request);
    }
}
