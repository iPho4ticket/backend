package com.ipho.ticketservice.application.dto;

import java.util.UUID;

public record TicketRequestDto(Long userId,
                               UUID eventId,
                               String seatNumber,
                               Double price) {
}
