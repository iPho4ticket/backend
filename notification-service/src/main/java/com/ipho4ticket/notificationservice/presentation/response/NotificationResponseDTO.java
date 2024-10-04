package com.ipho4ticket.notificationservice.presentation.response;

import com.ipho4ticket.notificationservice.domain.model.NotificationStatus;
import com.ipho4ticket.notificationservice.domain.model.NotificationType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotificationResponseDTO {
    private UUID notificationId;
    private Long userId;
    private NotificationType type;
    private String message;
    private NotificationStatus status;
}
