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
public class EventDto implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 엔티티에서 DTO로 변환하는 메서드
    public static EventDto of(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();
    }
}
