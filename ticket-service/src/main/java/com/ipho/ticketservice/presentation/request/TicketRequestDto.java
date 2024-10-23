package com.ipho.ticketservice.presentation.request;

import java.util.UUID;

public record TicketRequestDto(UUID eventId,
                               String seatNumber,
                               Double price) {
}
