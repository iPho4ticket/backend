package com.ipho4ticket.notificationservice.presentation.request;

import com.ipho4ticket.notificationservice.domain.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationRequestDTO {
    private Long userId;
    private NotificationType type;
    private String message;
}
