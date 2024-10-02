package com.ipho.ticketservice.infrastructure.repository;

import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaTicketRepository  extends TicketRepository, JpaRepository<Ticket, UUID> {
}
