package com.ipho.ticketservice.application.service;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import com.ipho.ticketservice.application.event.CancelTicketEvent;
import com.ipho.ticketservice.application.event.SeatBookingEvent;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.domain.model.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventService eventService;

    @Transactional
    public TicketResponseDto reservationTicket(TicketRequestDto dto) {
        Ticket ticket = new Ticket(dto.userId(), dto.eventId(), dto.seatNumber(), dto.price());
        ticketRepository.save(ticket);
        eventService.publishSeatBookingEvent(new SeatBookingEvent());

        return TicketResponseDto.createTicket(ticket);
    }

    @Transactional(readOnly = true)
    public TicketInfoDto searchTicketInfo(UUID ticketId) {
        return TicketInfoDto.of(ticketRepository.findByUuid(ticketId).orElseThrow(() -> new IllegalArgumentException("not found ticket")));
    }

    @Transactional
    public TicketResponseDto cancelTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findByUuidAndStatusNot(ticketId, TicketStatus.CANCELED).orElseThrow(() -> new IllegalArgumentException("not found ticket or already canceled"));
        ticket.cancel();

        eventService.publishCancelTicket(new CancelTicketEvent(ticket.getSeatId(), ticket.getEventId(), ticket.getSeatNumber(), ticket.getPrice()));
        return TicketResponseDto.cancelTicket(ticket);
    }


}
