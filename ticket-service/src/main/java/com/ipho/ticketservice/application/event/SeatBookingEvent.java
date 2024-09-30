package com.ipho.ticketservice.application.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatBookingEvent {

    private Long userId;
    private UUID eventId;
    private String seatNumber;
    private Double price;
}
