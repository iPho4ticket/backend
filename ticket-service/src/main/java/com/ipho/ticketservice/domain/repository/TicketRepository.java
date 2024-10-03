package com.ipho.ticketservice.domain.repository;


import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findByUuid(UUID id);
    Optional<Ticket> findByUuidAndStatusNot(UUID id, TicketStatus status);

}
