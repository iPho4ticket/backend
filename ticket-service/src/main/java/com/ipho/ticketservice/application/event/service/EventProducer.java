package com.ipho.ticketservice.application.event.service;

import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketTopic;
import com.ipho.ticketservice.infrastructure.messaging.DynamicKafkaListener;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatBookingEvent(SeatBookingEvent event) {
        kafkaTemplate.send(TicketTopic.SEAT_BOOKING.getTopic() + "-" + event.getEventId(), event);
    }

    public void publishCancelTicket(CancelTicketEvent event) {
        kafkaTemplate.send(TicketTopic.CANCEL_TICKET.getTopic() + "-" + event.getEventId(), event);
    }
}
