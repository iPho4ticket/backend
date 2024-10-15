package com.ipho.ticketservice.application.event;

import com.ipho.common.dto.CancelTicketEvent;
import com.ipho.common.dto.ConfirmSeatEvent;
import com.ipho.common.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.TicketTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    public void publishConfirmSeat(ConfirmSeatEvent event) {
        kafkaTemplate.send(TicketTopic.CONFIRM_SEAT.getTopic() + "-" + event.getEventId(), event);
    }
}
