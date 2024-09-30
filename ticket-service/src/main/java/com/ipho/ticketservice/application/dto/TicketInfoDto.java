package com.ipho.ticketservice.application.dto;

import com.ipho.ticketservice.domain.Ticket;

import java.util.UUID;

public record TicketInfoDto(UUID ticketId,
                            Long userId,
                            UUID eventId,
                            String seatNumber,
                            String status,
                            String reservationTime,
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
                ticket.getPrice(),
                "Ticket details retrieved successfully.");
    }
}
