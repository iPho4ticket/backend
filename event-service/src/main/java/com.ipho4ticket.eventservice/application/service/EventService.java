package com.ipho4ticket.eventservice.application.service;

import com.ipho4ticket.eventservice.application.dto.EventResponseDto;
import com.ipho4ticket.eventservice.application.service.exception.*;
import com.ipho4ticket.eventservice.domain.model.Event;
import com.ipho4ticket.eventservice.domain.model.QEvent;
import com.ipho4ticket.eventservice.domain.repository.EventRepository;
import com.ipho4ticket.eventservice.presentation.request.EventRequestDto;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    // 이벤트 생성
    @Transactional
    public EventResponseDto createEvent(EventRequestDto request) {
        Event event=new Event(request.title(),request.description(),request.date(),request.startTime(),request.endTime());
        eventRepository.save(event);

        return toResponseDTO(event);
    }

    // 이벤트 검색
    public Page<EventResponseDto> searchEvents(String title, String description, Pageable pageable) {
        QEvent qEvent = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();

        // 제목 조건 추가
        if (title != null && !title.isEmpty()) {
            builder.and(qEvent.title.containsIgnoreCase(title));
        }

        // 설명 조건 추가
        if (description != null && !description.isEmpty()) {
            builder.and(qEvent.description.containsIgnoreCase(description));
        }
        Page<Event> events= eventRepository.findAll(builder, pageable);
        if (events.getTotalElements()==0){
            throw new SearchNotExistsException("해당 검색 결과가 없습니다.");
        }

        return events.map(this::toResponseDTO);
    }

    // 이벤트 수정
    @Transactional
    public EventResponseDto updateEvent(UUID id, EventRequestDto request) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));
        event.update(request.title(),request.description(),request.date(),request.startTime(),request.endTime());
        eventRepository.save(event);
        return toResponseDTO(event);
    }

    public void deleteEvent(UUID id) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));
        eventRepository.delete(event);
    }

    public EventResponseDto getEvent(UUID id) {
        Event event= eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));
        return toResponseDTO(event);
    }

    public Page<EventResponseDto> getEvents(Pageable pageable) {
        Page<Event> events=eventRepository.findAll(pageable);
        return events.map(this::toResponseDTO);
    }

    private EventResponseDto toResponseDTO(Event event){
        return EventResponseDto.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();

    }
}
