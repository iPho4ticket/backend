package com.ipho.ticketservice.application.event.service;

import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatBookingEvent(SeatBookingEvent event) {
        kafkaTemplate.send(TicketTopic.SEAT_BOOKING.getTopic(), event);
    }

    public void publishCancelTicket(CancelTicketEvent event) {
        kafkaTemplate.send(TicketTopic.CANCEL_TICKET.getTopic(), event);
    }
}
