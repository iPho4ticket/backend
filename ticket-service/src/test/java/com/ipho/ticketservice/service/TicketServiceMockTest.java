package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.service.EventProducer;
import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.domain.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketServiceMockTest {

    @Autowired
    private TicketService ticketService;
    private TicketRepository ticketRepository;
    private EventProducer eventProducer;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventProducer = mock(EventProducer.class);
        ticketService = new TicketService(ticketRepository, eventProducer);
    }

    @Test
    @DisplayName("티켓 예매")
    void reservationTicket() {
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket savedTicket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketResponseDto responseDto = ticketService.reservationTicket(requestDto);

        assertNotNull(responseDto);
        assertEquals(savedTicket.getUuid(), responseDto.ticketId());
        verify(eventProducer, times(1)).publishSeatBookingEvent(any(SeatBookingEvent.class));
    }

    @Test
    @DisplayName("티켓 정보 조회")
    void searchTicketInfo() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        when(ticketRepository.findByUuid(ticket.getUuid())).thenReturn(Optional.of(ticket));

        TicketInfoDto infoDto = ticketService.searchTicketInfo(ticket.getUuid());
        
        assertNotNull(infoDto);
        assertEquals(ticket.getSeatNumber(), infoDto.seatNumber());
    }

    @Test
    @DisplayName("티켓 예매 취소")
    void cancelTicket() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        when(ticketRepository.findByUuidAndStatusNot(ticket.getUuid(), TicketStatus.CANCELED)).thenReturn(Optional.of(ticket));

        TicketResponseDto responseDto = ticketService.cancelTicket(ticket.getUuid());

        assertNotNull(responseDto);
        assertEquals(ticket.getUuid(), responseDto.ticketId());
        assertEquals(ticket.getStatus().toString(), responseDto.ticketStatus());

        verify(eventProducer, times(1)).publishCancelTicket(any(CancelTicketEvent.class));
    }

    @Test
    @DisplayName("내부 API - 유효한 티켓 검증 ( Success Case )")
    void validTicket_Success() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        ticket.pending();
        when(ticketRepository.findByValidationTicket(ticket.getUuid(), TicketStatus.PENDING)).thenReturn(Optional.of(ticket));

        ValidationResponse validationResponse = ticketService.validateTicket(ticket.getUuid(), 1L);

        assertNotNull(validationResponse);
        assertTrue(validationResponse.success());
        assertEquals(validationResponse.message(), "valid ticket");
    }



    @Test
    @DisplayName("내부 API - 유효한 티켓 검증 ( Failure Case 1. not pending )")
    void validTicket_Failure_NotPending() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);

        when(ticketRepository.findByValidationTicket(ticket.getUuid(), TicketStatus.PENDING)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.validateTicket(ticket.getUuid(), 1L);
        });
        assertEquals(exception.getMessage(), "not processing ticket");
    }

    @Test
    @DisplayName("내부 API - 유효한 티켓 검증 ( Failure Case 2. no authority userId ")
    void validTicket_Failure_NoAuthorityUserId() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        ticket.pending();

        when(ticketRepository.findByValidationTicket(ticket.getUuid(), TicketStatus.PENDING)).thenReturn(Optional.of(ticket));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.validateTicket(ticket.getUuid(), 2L);
        });
        assertEquals(exception.getMessage(), "no authority userId");
        System.out.println("exception = " + exception.getMessage());
    }

}
