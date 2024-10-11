package com.ipho.ticketservice.application.event.service;

import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketTopic;
import com.ipho.ticketservice.infrastructure.messaging.DynamicKafkaListener;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DynamicKafkaListener kafkaListener;

    public void publishSeatBookingEvent(UUID eventId, SeatBookingEvent event) {
//        kafkaListener.startListener(TicketTopic.SEAT_BOOKING.getTopic(), eventId);
        kafkaTemplate.send(TicketTopic.SEAT_BOOKING.getTopic() + "-" + eventId, event);
    }

    public void publishCancelTicket(UUID eventId, CancelTicketEvent event) {
//        kafkaListener.startListener(TicketTopic.CANCEL_TICKET.getTopic(), eventId);
        kafkaTemplate.send(TicketTopic.CANCEL_TICKET.getTopic() + "-" + eventId, event);
    }
}
