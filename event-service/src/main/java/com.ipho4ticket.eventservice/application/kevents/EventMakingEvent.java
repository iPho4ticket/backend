package com.ipho4ticket.eventservice.application.kevents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventMakingEvent {
    private UUID eventId;
    private String title;
    private String description;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topics;
}
