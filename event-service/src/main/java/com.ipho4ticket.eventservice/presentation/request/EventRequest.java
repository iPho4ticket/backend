package com.ipho4ticket.eventservice.presentation.request;

import com.ipho4ticket.eventservice.domain.model.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventRequest {
    private String title;
    private String description;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 엔티티로 변환하는 메서드
    public Event toEntity() {
        return Event.builder()
                .title(this.title)
                .description(this.description)
                .date(this.date)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
    }

    // 엔티티로 변환하는 메서드
    public Event toEntityTest() {
        return Event.builder()
                .id(UUID.randomUUID()) // UUID를 여기서 생성
                .title(this.title)
                .description(this.description)
                .date(this.date)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
    }
}
