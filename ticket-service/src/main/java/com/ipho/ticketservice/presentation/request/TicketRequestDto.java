package com.ipho.ticketservice.presentation.request;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketRequestDto(UUID eventId,
                               Long userId,
                               String seatNumber,
                               BigDecimal price) {
}
