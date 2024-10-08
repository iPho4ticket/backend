package com.ipho.ticketservice.application.dto;

import com.ipho.ticketservice.domain.model.Ticket;

import java.util.UUID;

public record TicketInfoDto(UUID ticketId,
                            Long userId,
                            UUID eventId,
                            String seatNumber,
                            String status,
                            String reservationTime,
                            String expirationTime,
                            Double price,
                            String message) {

    public static TicketInfoDto of(Ticket ticket) {
        return new TicketInfoDto(
                ticket.getUuid(),
                ticket.getUserId(),
                ticket.getEventId(),
                ticket.getSeatNumber(),
                ticket.getStatus().toString(),
                ticket.getReservationTime().toString(),
                ticket.getExpirationTime().toString(),
                ticket.getPrice(),
                "Ticket details retrieved successfully.");
    }
}
