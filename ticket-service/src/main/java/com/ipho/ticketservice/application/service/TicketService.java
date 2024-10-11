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
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        return TicketInfoDto.of(ticketRepository.findByUuid(ticketId).orElseThrow(() -> new IllegalArgumentException("not found ticket")));
    }

    @Transactional
    public TicketResponseDto cancelTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findByUuidAndStatusNot(ticketId, TicketStatus.CANCELED).orElseThrow(() -> new IllegalArgumentException("not found ticket or already canceled"));
        ticket.cancel();

        eventProducer.publishCancelTicket(new CancelTicketEvent(ticket.getSeatId(), ticket.getEventId(), ticket.getSeatNumber(), ticket.getPrice()));
        return TicketResponseDto.cancelTicket(ticket);
    }

    @Transactional(readOnly = true)
    public ValidationResponse validateTicket(UUID ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByValidationTicket(ticketId, TicketStatus.PENDING).orElseThrow(() -> new IllegalArgumentException("not processing ticket"));
        if (!ticket.getUserId().equals(userId)) throw new IllegalArgumentException("no authority userId");

        return new ValidationResponse(true, "valid ticket");
        /*
         * 결제 완료 ( CONFIRMED ) 가 되기 위한 Ticket 의 상태 조건

         * 1. PENDING 상태
         * 2. expirationTime > current Time
         * 아직 필드 없음. IsDelete == false
         */
    }

    @Transactional
    public ValidationResponse completePayment(UUID ticketId) {
        Ticket ticket = ticketRepository.findByValidationTicket(ticketId, TicketStatus.PENDING).orElseThrow(() -> new IllegalArgumentException("not found valid ticket by ticketId"));
        ticket.completePayment();
        return new ValidationResponse(true, "complete payment");
    }


    // 지울 용도 ( 시나리오를 위한 쓰레기 )
    @Transactional
    public String pending(UUID ticketId) {
        Ticket ticket = ticketRepository.findByUuid(ticketId).get();
        ticket.pending();
        return "success";
    }

    @Transactional
    public void ticketMaking(TicketMakingEvent event) {
        Ticket ticket = ticketRepository.findByUuid(event.getTicketId()).orElseThrow(() -> new IllegalArgumentException("not found ticket by uuid"));
        ticket.pending();
    }
}
