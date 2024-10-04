package com.ipho4ticket.notificationservice;


import com.ipho4ticket.notificationservice.application.service.NotificationService;
import com.ipho4ticket.notificationservice.domain.model.Notification;
import com.ipho4ticket.notificationservice.domain.model.NotificationStatus;
import com.ipho4ticket.notificationservice.domain.model.NotificationType;
import com.ipho4ticket.notificationservice.domain.repository.NotificationRepository;
import com.ipho4ticket.notificationservice.presentation.request.NotificationRequestDTO;
import com.ipho4ticket.notificationservice.presentation.response.NotificationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    public void setUp() {
        notification = Notification.builder()
            .userId(1L)
            .type(NotificationType.EMAIL)
            .message("Test message")
            .status(NotificationStatus.COMPLETED)
            .build();
    }

    // 알림 생성 테스트
    @Test
    public void testCreateNotification() throws Exception {
        NotificationRequestDTO request = new NotificationRequestDTO(1L, NotificationType.EMAIL, "Test message");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponseDTO response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(NotificationType.EMAIL, response.getType());
        assertEquals("Test message", response.getMessage());
        assertEquals(NotificationStatus.COMPLETED, response.getStatus());

        // Mock verification
        verify(notificationRepository, times(1)).save(any(Notification.class));

        // 리플렉션을 이용한 private 필드 값 변경 및 검증
        Field statusField = Notification.class.getDeclaredField("status");
        statusField.setAccessible(true);  // private 필드에 접근 가능하도록 설정
        NotificationStatus status = (NotificationStatus) statusField.get(notification);
        assertEquals(NotificationStatus.COMPLETED, status);
    }

    // 알림 조회 테스트
    @Test
    public void testGetNotification() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        NotificationResponseDTO response = notificationService.getNotification(notificationId);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(NotificationType.EMAIL, response.getType());
        assertEquals("Test message", response.getMessage());
        assertEquals(NotificationStatus.COMPLETED, response.getStatus());

        verify(notificationRepository, times(1)).findById(notificationId);
    }

    // 알림 삭제 테스트
    @Test
    public void testDeleteNotification() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        NotificationResponseDTO response = notificationService.deleteNotification(notificationId);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(NotificationType.EMAIL, response.getType());
        assertEquals("Test message", response.getMessage());
        assertEquals(NotificationStatus.COMPLETED, response.getStatus());

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).delete(notification);
    }
}

