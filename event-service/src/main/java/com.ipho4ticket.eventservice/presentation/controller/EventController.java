package com.ipho4ticket.eventservice.presentation.controller;

import com.ipho4ticket.eventservice.application.dto.EventDto;
import com.ipho4ticket.eventservice.application.service.EventService;
import com.ipho4ticket.eventservice.domain.model.Event;
import com.ipho4ticket.eventservice.presentation.request.EventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<EventDto> createEvent(@RequestBody EventRequest request){
        Event event=eventService.createEvent(request.toEntity());
        return ResponseEntity.ok(EventDto.of(event));
    }
    // 이벤트 검색
    @GetMapping("/search")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            Pageable pageable){
        Page<Event> events=eventService.searchEvents(title,description,pageable);
        return ResponseEntity.ok(events.map(EventDto::of));
    }

    // 이벤트 수정
    @PatchMapping("/{event_id}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable("event_id") UUID id,
            @RequestBody EventRequest request){
        Event event=eventService.updateEvent(id,request);
        return ResponseEntity.ok(EventDto.of(event));
    }

    // 이벤트 삭제
    @DeleteMapping("/{event_id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("event_id") UUID id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // 이벤트 조회
    @GetMapping("/{event_id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable("event_id") UUID id){
        Event event = eventService.getEvent(id);
        return ResponseEntity.ok(EventDto.of(event));
    }

    // 이벤트 전체 조회
    @GetMapping
    public ResponseEntity<Page<EventDto>> getEvents(Pageable pageable){
        Page<Event> events=eventService.getEvents(pageable);
        return ResponseEntity.ok(events.map(EventDto::of));
    }
}
