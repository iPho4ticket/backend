package com.ipho.ticketservice.application.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatBookingEvent {
    private UUID ticketId;
    private UUID eventId;
    private Long userId;
    private String seatNumber;
}