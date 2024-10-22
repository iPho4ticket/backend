package com.ipho4ticket.seatservice.presentation.controller;

import com.ipho.common.dto.SeatRequestDto;
import com.ipho4ticket.seatservice.application.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SeatInternalController {

    private final SeatService seatService;
//    private final DynamicKafkaListener dynamicKafkaListener;
//
//    // 동적 토픽 생성
//    @PostMapping("/api/v1/internal/seats/subscribe")
//    public ResponseEntity<String> subscribe(@RequestParam String topic, @RequestParam UUID eventId) {
//        dynamicKafkaListener.startListener(topic, eventId);
//        return ResponseEntity.ok("Subscribed to topic: " + topic);
//    }

    @PostMapping("/api/v1/internal/seat")
    public void getSeat(@RequestBody SeatRequestDto request){
        seatService.checkSeat(request);
    }
}
