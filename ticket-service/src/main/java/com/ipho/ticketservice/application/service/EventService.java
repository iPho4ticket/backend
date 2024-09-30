package com.ipho.ticketservice.application.service;

import com.ipho.ticketservice.application.event.CancelTicketEvent;
import com.ipho.ticketservice.application.event.SeatBookingEvent;
import com.ipho.ticketservice.application.event.TicketTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatBookingEvent(SeatBookingEvent event) {
        kafkaTemplate.send(TicketTopic.SEAT_BOOKING.getTopic(), event);
    }

    public void publishCancelTicket(CancelTicketEvent event) {
        kafkaTemplate.send(TicketTopic.CANCEL_TICKET.getTopic(), event);
    }
}
