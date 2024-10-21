package com.ipho4ticket.eventservice;

import com.ipho4ticket.eventservice.application.dto.EventResponseDto;
import com.ipho4ticket.eventservice.application.service.EventService;
import com.ipho4ticket.eventservice.domain.model.Event;
import com.ipho4ticket.eventservice.domain.repository.EventRepository;
import com.ipho4ticket.eventservice.presentation.request.EventRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;;
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
    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    private EventRequestDto eventRequestDto;
    private Event event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        eventRequestDto = new EventRequestDto(
                "Sample Event",
                "Sample Description",
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        );

        event = new Event(eventRequestDto.title(),eventRequestDto.description(),eventRequestDto.date(),eventRequestDto.startTime(),eventRequestDto.endTime());
        event = Event.builder()
                .eventId(UUID.randomUUID()) // Set eventId here
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();
    }

    @Test
    void createEvent() {
        Event event = new Event(eventRequestDto.title(), eventRequestDto.description(), eventRequestDto.date(), eventRequestDto.startTime(), eventRequestDto.endTime());

        when(eventRepository.save(any(Event.class))).thenReturn(event);
        EventResponseDto response=eventService.createEvent(eventRequestDto);

        assertNotNull(response);
        assertEquals(event.getTitle(),response.getTitle());
    }

    @Test
    void updateEvent() {
        UUID eventId = event.getEventId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponseDto response = eventService.updateEvent(eventId, eventRequestDto);

        assertNotNull(response);
        assertEquals(event.getTitle(), response.getTitle());
        verify(eventRepository, times(1)).findById(eventId);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_NotFound() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(eventId, eventRequestDto);
        });

        assertEquals(eventId + "는 찾을 수 없는 이벤트 아이디입니다.", exception.getMessage());
    }

    @Test
    void deleteEvent() {
        UUID eventId = event.getEventId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    void deleteEvent_NotFound() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(eventId);
        });

        assertEquals(eventId + "는 찾을 수 없는 이벤트 아이디입니다.", exception.getMessage());
    }

    @Test
    void getEvent() {
        UUID eventId = event.getEventId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventResponseDto response = eventService.getEvent(eventId);

        assertNotNull(response);
        assertEquals(event.getTitle(), response.getTitle());
        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    void getEvent_NotFound() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEvent(eventId);
        });

        assertEquals(eventId + "는 찾을 수 없는 이벤트 아이디입니다.", exception.getMessage());
    }

    @Test
    void getEvents() {
        Page<Event> eventPage = new PageImpl<>(Arrays.asList(event));
        when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);

        Page<EventResponseDto> response = eventService.getEvents(Pageable.ofSize(10));

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(event.getTitle(), response.getContent().get(0).getTitle());
        verify(eventRepository, times(1)).findAll(any(Pageable.class));
    }
}
