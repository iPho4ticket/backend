package com.ipho.ticketservice.domain.repository;


import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findByUuid(UUID id);
    Optional<Ticket> findByUuidAndStatusNot(UUID id, TicketStatus status);

    @Query("select t from Ticket t where t.uuid = :id and t.status = :status and t.expirationTime > CURRENT TIMESTAMP ")
    Optional<Ticket> findByValidationTicket(UUID id, TicketStatus status);

    @Query("select t from Ticket t where t.uuid = :id and t.status != :status and t.expirationTime > CURRENT TIMESTAMP ")
    Optional<Ticket> findByValidationCancelTicket(UUID id, TicketStatus status);

}
