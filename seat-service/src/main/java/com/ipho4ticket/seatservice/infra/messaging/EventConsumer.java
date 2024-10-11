package com.ipho4ticket.seatservice.infra.messaging;

import com.ipho4ticket.seatservice.application.events.SeatBookingEvent;
import com.ipho4ticket.seatservice.application.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {
    private final SeatService seatService;

    @KafkaListener(topics="seat-booking",groupId="${spring.application.name}")
    public void handleSeatBooking(String message){
        SeatBookingEvent event=EventSerializer.deserialize(message, SeatBookingEvent.class);
        seatService.checkSeat(event);
    }
}
