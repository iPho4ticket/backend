package com.ipho4ticket.notificationservice.presentation.controller;

import com.ipho4ticket.notificationservice.application.service.NotificationService;
import com.ipho4ticket.notificationservice.presentation.request.NotificationRequestDTO;
import com.ipho4ticket.notificationservice.presentation.response.NotificationResponseDTO;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 알림 생성 (USER 권한 필요)
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
        @RequestBody NotificationRequestDTO request) {
        NotificationResponseDTO response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 알림 조회 (MANAGER 권한 필요)
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDTO> getNotification(
        @PathVariable UUID notificationId) {
        NotificationResponseDTO response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    // 알림 삭제 (MANAGER 권한 필요)
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}

