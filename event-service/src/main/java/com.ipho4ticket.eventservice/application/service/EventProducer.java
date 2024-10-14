package com.ipho4ticket.eventservice.application.service;

import com.ipho4ticket.eventservice.application.kevents.EventMakingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEventMakingEvent(EventMakingEvent event) {
        kafkaTemplate.send("event-making" + "-" + event.getEventId(), event);
    }
}