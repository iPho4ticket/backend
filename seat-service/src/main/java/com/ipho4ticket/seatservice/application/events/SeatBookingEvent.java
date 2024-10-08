package com.ipho4ticket.seatservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatBookingEvent {
    // 변경 필요
    private UUID ticketId;
    private UUID seatId;
}
