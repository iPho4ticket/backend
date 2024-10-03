package com.ipho.ticketservice.presentation.response;

import com.ipho.ticketservice.domain.model.Ticket;

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
