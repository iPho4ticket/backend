package com.ipho4ticket.notificationservice.application.service;

import com.ipho4ticket.notificationservice.domain.model.Notification;
import com.ipho4ticket.notificationservice.domain.model.NotificationStatus;
import com.ipho4ticket.notificationservice.domain.repository.NotificationRepository;
import com.ipho4ticket.notificationservice.presentation.request.NotificationRequestDTO;
import com.ipho4ticket.notificationservice.presentation.response.NotificationResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림 생성
    @Transactional
    public NotificationResponseDTO createNotification(NotificationRequestDTO request) {
        // ID를 제외한 빌더 패턴 사용
        Notification notification = Notification.builder()
            .userId(request.getUserId())
            .type(request.getType())
            .message(request.getMessage())
            .status(NotificationStatus.COMPLETED)  // 초기 상태
            .build();

        notificationRepository.save(notification);
        return toResponseDTO(notification);
    }

    // 알림 조회
    @Transactional(readOnly = true)
    public NotificationResponseDTO getNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        return toResponseDTO(notification);
    }

    // 알림 삭제
    @Transactional
    public NotificationResponseDTO deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notificationRepository.delete(notification);

        return toResponseDTO(notification);
    }

    public NotificationResponseDTO toResponseDTO(Notification notification) {
        return new NotificationResponseDTO(
            notification.getNotificationId(),
            notification.getUserId(),
            notification.getType(),
            notification.getMessage(),
            notification.getStatus()
        );
    }
}
