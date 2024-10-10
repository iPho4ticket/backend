package com.ipho.ticketservice.application.event.service;

import com.ipho.ticketservice.application.event.dto.TicketMakingEvent;
import com.ipho.ticketservice.application.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final TicketService ticketService;

    @KafkaListener(topics = "ticket-making", groupId = "${spring.application.name}")
    public void handleTicketMaking(TicketMakingEvent event) {
        ticketService.ticketMaking(event);
    }
}
