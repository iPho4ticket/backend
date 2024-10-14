package com.ipho4ticket.seatservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatBookingEvent {
    private UUID ticketId;
    private UUID seatId;
    private UUID eventId;
    private Long userId;
    private String seatNumber;
}