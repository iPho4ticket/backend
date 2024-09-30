package com.ipho.ticketservice.application.dto;

import com.ipho.ticketservice.domain.Ticket;

import java.util.UUID;

public record TicketResponseDto(UUID ticketId,
                                String ticketStatus,
                                String message){

    public static TicketResponseDto createTicket(Ticket ticket) {
        return new TicketResponseDto(ticket.getUuid(), ticket.getStatus().toString(), "Ticket reserved successfully.");
    }

    public static TicketResponseDto cancelTicket(Ticket ticket) {
        return new TicketResponseDto(ticket.getUuid(), ticket.getStatus().toString(), "Ticket canceled successfully.");
    }
}
