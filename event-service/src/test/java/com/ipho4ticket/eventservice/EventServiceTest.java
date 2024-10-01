package com.ipho4ticket.eventservice;

import com.ipho4ticket.eventservice.application.service.EventService;
import com.ipho4ticket.eventservice.domain.model.Event;
import com.ipho4ticket.eventservice.domain.repository.EventRepository;
import com.ipho4ticket.eventservice.presentation.request.EventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private List<Event> events;  // 이벤트 목록
    private UUID eventId;  // UUID를 클래스 레벨에서 선언

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 이벤트 목록 초기화
        events = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            eventId = UUID.randomUUID();  // UUID 생성
            Event event = Event.builder()
                    .id(eventId)  // UUID 설정
                    .title("Sample Event Title " + i)
                    .description("This is a sample event description " + i + ".")
                    .date(LocalDate.parse("2024-09-30"))
                    .startTime(LocalDateTime.parse("2024-09-30T10:00:00"))
                    .endTime(LocalDateTime.parse("2024-09-30T12:00:00"))
                    .build();
            events.add(event);  // 이벤트 목록에 추가
        }

        // Mock 설정
        when(eventRepository.findAllByIsDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(events)); // 이벤트 페이지 설정

        // 첫 번째 이벤트에 대한 Mock 설정
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(events.get(0)));
    }

    @Test
    @DisplayName("이벤트 생성")
    public void createEvent() {
        // Given
        EventRequest newEventRequest = EventRequest.builder()
                .title("New Sample Event")
                .description("This is a new sample event description.")
                .date(LocalDate.parse("2024-09-30"))
                .startTime(LocalDateTime.parse("2024-09-30T10:00:00"))
                .endTime(LocalDateTime.parse("2024-09-30T12:00:00"))
                .build();

        // 새로운 이벤트 객체 생성
        Event createdEvent = Event.builder()
                .id(UUID.randomUUID())
                .title(newEventRequest.getTitle())
                .description(newEventRequest.getDescription())
                .date(newEventRequest.getDate())
                .startTime(newEventRequest.getStartTime())
                .endTime(newEventRequest.getEndTime())
                .build();

        // When
        when(eventRepository.save(any(Event.class))).thenReturn(createdEvent);
        Event result = eventService.createEvent(newEventRequest.toEntityTest()); // 여기에 `toEntityTest()`가 정의되어 있어야 함

        // Then
        assertNotNull(result);
        assertEquals(createdEvent.getId(), result.getId());
        assertEquals(createdEvent.getTitle(), result.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("이벤트 전체조회")
    public void getEvents() {
        // Given
        Pageable pageable = Pageable.ofSize(10);

        // When
        Page<Event> result = eventService.getEvents(pageable);

        // Then
        assertNotNull(result);
        assertEquals(events.size(), result.getTotalElements());

        // 이벤트 출력
        result.getContent().forEach(e -> System.out.println("Event ID: " + e.getId() +
                ", Title: " + e.getTitle() +
                ", Description: " + e.getDescription()));

        verify(eventRepository, times(1)).findAllByIsDeletedFalse(pageable);
    }

    @Test
    @DisplayName("이벤트 조회")
    public void getEvent() {
        // When
        Event foundEvent = eventService.getEvent(events.get(0).getId()); // 수정: 실제 ID 사용

        // Then
        assertNotNull(foundEvent);
        assertEquals(events.get(0).getId(), foundEvent.getId());
        System.out.println("이벤트 조회 결과: " + foundEvent.getId());
        verify(eventRepository, times(1)).findById(events.get(0).getId());
    }
}
