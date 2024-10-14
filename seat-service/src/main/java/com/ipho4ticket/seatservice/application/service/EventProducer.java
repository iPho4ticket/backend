package com.ipho4ticket.seatservice.application.service;

import com.ipho4ticket.seatservice.application.events.TicketMakingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTicketMakingEvent(TicketMakingEvent event) {
        kafkaTemplate.send("ticket-making", EventSerializer.serialize(event));
    }
}

