package com.ipho4ticket.eventservice.presentation.controller;

import com.ipho4ticket.eventservice.application.dto.EventResponseDto;
import com.ipho4ticket.eventservice.application.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/event")
public class EventInternalController {
    private final EventService eventService;

    @GetMapping("/{event_id}")
    public ResponseEntity<EventResponseDto> sendEventInfo(
            @PathVariable("event_id") UUID eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }
}
