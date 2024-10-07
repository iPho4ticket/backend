package com.ipho4ticket.eventservice.presentation.controller;

import com.ipho4ticket.eventservice.application.dto.EventResponseDto;
import com.ipho4ticket.eventservice.application.service.EventService;
import com.ipho4ticket.eventservice.domain.model.Event;
import com.ipho4ticket.eventservice.presentation.request.EventRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    // 이벤트 생성
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventRequestDto request){
        EventResponseDto event=eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
    // 이벤트 검색
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponseDto>> searchEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable= PageRequest.of(page,size);
        Page<EventResponseDto> events=eventService.searchEvents(title,description,pageable);
        return ResponseEntity.ok(events);
    }

    // 이벤트 수정
    @PatchMapping("/{event_id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable("event_id") UUID id,
            @RequestBody EventRequestDto request){
        EventResponseDto event=eventService.updateEvent(id,request);
        return ResponseEntity.ok(event);
    }

    // 이벤트 삭제
    @DeleteMapping("/{event_id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("event_id") UUID id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // 이벤트 조회
    @GetMapping("/{event_id}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable("event_id") UUID id){
        EventResponseDto event = eventService.getEvent(id);
        return ResponseEntity.ok(event);
    }

    // 이벤트 전체 조회
    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable=PageRequest.of(page,size);
        Page<EventResponseDto> events=eventService.getEvents(pageable);
        return ResponseEntity.ok(events);
    }
}
