package com.ipho.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketMakingEvent {
    private UUID eventId;
    private UUID ticketId;
    private UUID seatId;
    private String eventName;
    private String seatNumber;
    private BigDecimal price;
}
