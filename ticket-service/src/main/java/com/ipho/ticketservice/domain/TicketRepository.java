package com.ipho.ticketservice.domain;

import com.ipho.ticketservice.domain.Ticket;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findByUuid(UUID id);
    Optional<Ticket> findByUuidAndStatusNot(UUID id, TicketStatus status);

}
