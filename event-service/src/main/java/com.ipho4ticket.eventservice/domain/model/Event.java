package service.event.domain.model;

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
public class Event extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name="event_id")
    private UUID id;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private LocalDate date;

    @Column(nullable=false,name="start_time")
    private LocalDateTime startTime;

    @Column(nullable=false,name="end_time")
    private LocalDateTime endTime;

    public void update(String title, String description, LocalDate date, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
