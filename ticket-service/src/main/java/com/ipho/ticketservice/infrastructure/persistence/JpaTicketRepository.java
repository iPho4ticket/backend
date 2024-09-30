package com.ipho.ticketservice.infrastructure.persistence;

import com.ipho.ticketservice.domain.Ticket;
import com.ipho.ticketservice.domain.TicketRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaTicketRepository  extends TicketRepository, JpaRepository<Ticket, UUID> {
}
