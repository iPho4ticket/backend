package com.ipho4ticket.seatservice.presentation.controller;

import com.ipho4ticket.seatservice.application.service.SeatService;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seats")
public class SeatController {

    private final SeatService seatService;

    // 좌석 생성
    @PostMapping
    public ResponseEntity<SeatResponseDto> createSeat(
            @RequestBody SeatRequestDto request){
        SeatResponseDto seat=seatService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    // 이벤트 좌석 전체 조회
    @GetMapping("/events/{event_id}")
    public ResponseEntity<Map<String, Object>> getAllSeats(
            @PathVariable("event_id") UUID event_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> seats = seatService.getAllSeats(event_id, pageable);
        return ResponseEntity.ok(seats);
    }

    // 이벤트 좌석 단건 조회
    @GetMapping("/{seat_id}")
    public ResponseEntity<SeatResponseDto> getSeat(@PathVariable UUID seat_id){
        SeatResponseDto seat=seatService.getSeat(seat_id);
        return ResponseEntity.ok(seat);
    }

    // 좌석 삭제
    @DeleteMapping("/{seat_id}")
    public ResponseEntity<?> deleteSeat(@PathVariable UUID seat_id){
        seatService.deleteSeat(seat_id);
        return ResponseEntity.noContent().build();
    }


}
