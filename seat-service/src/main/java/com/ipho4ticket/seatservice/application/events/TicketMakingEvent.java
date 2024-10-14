package com.ipho4ticket.seatservice.application.events;

import com.ipho4ticket.seatservice.domain.model.SeatStatus;
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
    private String eventName;
    private String seatNumber;
    private BigDecimal price;
}