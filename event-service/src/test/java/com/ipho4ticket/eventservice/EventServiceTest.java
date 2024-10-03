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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private List<Event> events;  // List of events
    private UUID eventId;  // Event UUID

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        events = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            eventId = UUID.randomUUID();
            Event event = Event.builder()
                    .id(eventId)
                    .title("Sample Event Title " + i)
                    .description("This is a sample event description " + i + ".")
                    .date(LocalDate.parse("2024-09-30"))
                    .startTime(LocalDateTime.parse("2024-09-30T10:00:00"))
                    .endTime(LocalDateTime.parse("2024-09-30T12:00:00"))
                    .build();
            events.add(event);
        }

        when(eventRepository.findAllByIsDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(events));
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(events.get(0)));
    }

    @Test
    @DisplayName("이벤트 생성")
    public void createEvent() {
        EventRequest newEventRequest = EventRequest.builder()
                .title("New Sample Event")
                .description("This is a new sample event description.")
                .date(LocalDate.parse("2024-09-30"))
                .startTime(LocalDateTime.parse("2024-09-30T10:00:00"))
                .endTime(LocalDateTime.parse("2024-09-30T12:00:00"))
                .build();

        Event createdEvent = Event.builder()
                .id(UUID.randomUUID())
                .title(newEventRequest.getTitle())
                .description(newEventRequest.getDescription())
                .date(newEventRequest.getDate())
                .startTime(newEventRequest.getStartTime())
                .endTime(newEventRequest.getEndTime())
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(createdEvent);

        Event result = eventService.createEvent(newEventRequest.toEntityTest());

        assertNotNull(result);
        assertEquals(createdEvent.getId(), result.getId());
        assertEquals(createdEvent.getTitle(), result.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("이벤트 전체조회")
    public void getEvents() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Event> result = eventService.getEvents(pageable);

        assertNotNull(result);
        assertEquals(events.size(), result.getTotalElements());

        result.getContent().forEach(e -> System.out.println("Event ID: " + e.getId() +
                ", Title: " + e.getTitle() +
                ", Description: " + e.getDescription()));

        verify(eventRepository, times(1)).findAllByIsDeletedFalse(pageable);
    }

    @Test
    @DisplayName("이벤트 조회")
    public void getEvent() {
        Event foundEvent = eventService.getEvent(events.get(0).getId());

        assertNotNull(foundEvent);
        assertEquals(events.get(0).getId(), foundEvent.getId());
        System.out.println("이벤트 조회 결과: " + foundEvent.getId());
        verify(eventRepository, times(1)).findById(events.get(0).getId());
    }

    @Test
    @DisplayName("이벤트 수정")
    public void updateEvent() {
        // Given
        UUID id = events.get(0).getId();
        EventRequest updateRequest = EventRequest.builder()
                .title("Updated Event Title")
                .description("Updated event description.")
                .date(LocalDate.parse("2024-09-30"))
                .startTime(LocalDateTime.parse("2024-09-30T10:00:00"))
                .endTime(LocalDateTime.parse("2024-09-30T12:00:00"))
                .build();

        when(eventRepository.findById(id)).thenReturn(Optional.of(events.get(0)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Event updatedEvent = eventService.updateEvent(id, updateRequest);

        // Then
        assertNotNull(updatedEvent);
        assertEquals(updateRequest.getTitle(), updatedEvent.getTitle());
        assertEquals(updateRequest.getDescription(), updatedEvent.getDescription());
        System.out.println("이벤트 수정 완료: ID = " + updatedEvent.getId() +
                ", Title = " + updatedEvent.getTitle() +
                ", Description = " + updatedEvent.getDescription());

        verify(eventRepository, times(1)).findById(id);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("이벤트 삭제")
    public void deleteEvent() {
        // Given
        UUID id = events.get(0).getId();
        when(eventRepository.findById(id)).thenReturn(Optional.of(events.get(0)));

        // When
        eventService.deleteEvent(id);

        // Then
        verify(eventRepository, times(1)).findById(id);
        verify(eventRepository, times(1)).save(any(Event.class)); // Verify save is called to persist changes
        System.out.println("이벤트 삭제 완료: ID = " + id);
    }
}
