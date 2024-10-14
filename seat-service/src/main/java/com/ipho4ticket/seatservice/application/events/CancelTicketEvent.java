package com.ipho4ticket.seatservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelTicketEvent {
    private UUID seatId;
    private UUID eventId;
    private String seatNumber;
    private Double price;
}
