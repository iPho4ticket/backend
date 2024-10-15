package com.ipho4ticket.seatservice.application.service;

import com.ipho.common.dto.TicketMakingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTicketMakingEvent(TicketMakingEvent event) {
        kafkaTemplate.send("ticket-making" + "-" + event.getEventId(), event);
    }
}
