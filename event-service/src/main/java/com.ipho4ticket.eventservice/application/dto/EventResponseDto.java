package com.ipho4ticket.eventservice.application.dto;

import com.ipho4ticket.eventservice.domain.model.Event;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventResponseDto{
    private UUID eventId;
    private String title;
    private String description;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public EventResponseDto(UUID eventId,String title,String description,LocalDate date,LocalDateTime startTime,LocalDateTime endTime){
        this.eventId=eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
