package com.ipho.ticketservice.application.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketMakingEvent {
    private UUID ticketId;
    private UUID seatId;
    private String seatNumber;
    private BigDecimal price;
}

