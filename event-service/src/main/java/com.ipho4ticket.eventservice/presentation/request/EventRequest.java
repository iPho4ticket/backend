package service.event.presentation.request;

import lombok.Builder;
import lombok.Getter;
import service.event.domain.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
