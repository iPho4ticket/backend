package com.ipho4ticket.eventservice.domain.model;

import com.ipho4ticket.eventservice.presentation.request.EventRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="events")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Event{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name="event_id")
    private UUID eventId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="description", nullable=false)
    private String description;

    @Column(name="date", nullable=false)
    private LocalDate date;

    @Column(nullable=false,name="start_time")
    private LocalDateTime startTime;

    @Column(nullable=false,name="end_time")
    private LocalDateTime endTime;

    public static Event create (EventRequestDto request) {
        return Event.builder()
                .title(request.title())
                .description(request.title())
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }
    public void update(EventRequestDto request){
        this.title = request.title();
        this.description = request.description();
        this.date = request.date();
        this.startTime = request.startTime();
        this.endTime = request.endTime();
    }
}
