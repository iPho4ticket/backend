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

    public Event(String title, String description, LocalDate date, LocalDateTime startTime, LocalDateTime endTime) {
        this.title=title;
        this.description=description;
        this.date=date;
        this.startTime=startTime;
        this.endTime=endTime;
    }

    public void update(String title, String description, LocalDate date, LocalDateTime startTime, LocalDateTime endTime){
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
