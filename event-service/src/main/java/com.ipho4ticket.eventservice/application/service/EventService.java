package service.event.application.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import service.event.domain.model.Event;
import service.event.domain.model.QEvent;
import service.event.domain.repository.EventRepository;
import service.event.presentation.request.EventRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Page<Event> searchEvents(String title, String description, Pageable pageable) {
        QEvent qEvent = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qEvent.isDeleted.isFalse());

        // 제목 조건 추가
        if (title != null && !title.isEmpty()) {
            builder.and(qEvent.title.containsIgnoreCase(title));
        }

        // 설명 조건 추가
        if (description != null && !description.isEmpty()) {
            builder.and(qEvent.description.containsIgnoreCase(description));
        }
        return eventRepository.findAll(builder, pageable);

    }

    public Event updateEvent(UUID id, EventRequest request) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));
        event.update(request.getTitle(),request.getDescription(),request.getDate(),request.getStartTime(),request.getEndTime());
        return eventRepository.save(event);
    }

    public void deleteEvent(UUID id) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));
        event.delete(id);
        eventRepository.save(event);
    }

    public Event getEvent(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(id+"는 찾을 수 없는 이벤트 아이디입니다."));

    }

    public Page<Event> getEvents(Pageable pageable) {
        return eventRepository.findAllByIsDeletedFalse(pageable);
    }
}
