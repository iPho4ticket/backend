package com.ipho4ticket.seatservice.presentation.controller;

import com.ipho4ticket.seatservice.infra.messaging.DynamicKafkaListener;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeatInternalController {

    private final DynamicKafkaListener dynamicKafkaListener;

    // 동적 토픽 생성
    @PostMapping("/api/v1/internal/seats/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String topic, @RequestParam UUID eventId) {
        dynamicKafkaListener.startListener(topic, eventId);
        return ResponseEntity.ok("Subscribed to topic: " + topic);
    }
}
