package com.ipho.ticketservice.application.service;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketMakingEvent;
import com.ipho.ticketservice.application.event.service.EventProducer;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import com.ipho.ticketservice.presentation.exception.TicketException;
import com.ipho.ticketservice.presentation.exception.ValidationException;
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ticket-service")
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventProducer eventProducer;

    @Transactional
    public TicketResponseDto reservationTicket(TicketRequestDto dto) {
        Ticket ticket = new Ticket(dto.userId(), dto.eventId(), dto.seatNumber(), dto.price());
        ticketRepository.save(ticket);
        eventProducer.publishSeatBookingEvent(new SeatBookingEvent(ticket.getUuid(), dto.userId(), dto.eventId(), dto.seatNumber(), dto.price()));

        return TicketResponseDto.createTicket(ticket);
    }

    @Transactional(readOnly = true)
    public TicketInfoDto searchTicketInfo(UUID ticketId) {
        return TicketInfoDto.of(ticketRepository.findByUuid(ticketId).orElseThrow(() -> new TicketException(HttpStatus.NOT_FOUND, "not found ticket by ticket id")));
    }

    @Transactional
    public TicketResponseDto cancelTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findByUuidAndStatusNot(ticketId, TicketStatus.CANCELED).orElseThrow(() -> new TicketException(HttpStatus.BAD_REQUEST, "not found ticket or already canceled"));
        ticket.cancel();

        eventProducer.publishCancelTicket(new CancelTicketEvent(ticket.getSeatId(), ticket.getEventId(), ticket.getSeatNumber(), ticket.getPrice()));
        return TicketResponseDto.cancelTicket(ticket);
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateTicket(UUID ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByValidationTicket(ticketId, TicketStatus.PENDING).orElseThrow(() -> new ValidationException("not processing ticket"));
        if (!ticket.getUserId().equals(userId)) throw new ValidationException("no authority userId");

        return new ValidationResponse(true, "valid ticket");
    }

    @Transactional
    public ValidationResponse completePayment(UUID ticketId) {
        Ticket ticket = ticketRepository.findByValidationTicket(ticketId, TicketStatus.PENDING).orElseThrow(() -> new ValidationException("not found valid ticket by ticketId"));
        ticket.completePayment();
        return new ValidationResponse(true, ticket.getEventName() + ":" + ticket.getSeatNumber());
    }

    @Transactional
    public void handleTicketMaking(TicketMakingEvent event) {
        Ticket ticket = ticketRepository.findByUuid(event.getTicketId()).orElseThrow(() -> new TicketException(HttpStatus.NOT_FOUND, "not found ticket"));
        ticket.addEventName(event.getEventName());
        ticket.pending();
        ticketRepository.save(ticket);
        log.debug("ticket making: {}", ticket);
    }

    @Transactional
    public ValidationResponse cancelPayment(UUID ticketId) {
        Ticket ticket = ticketRepository.findByUuid(ticketId).orElseThrow(() -> new ValidationException("not found ticket"));
        ticket.cancel();
        return new ValidationResponse(true, "success");
    }
}
