package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.event.EventProducer;
import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.infrastructure.client.SeatClientService;
import com.ipho.ticketservice.presentation.response.ValidationResponse;
import com.ipho.ticketservice.presentation.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketInternalMockTest {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private SeatClientService seatClientService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EventProducer eventProducer;


    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventProducer = mock(EventProducer.class);
        ticketService = new TicketService(ticketRepository, eventProducer, seatClientService);
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

        ValidationException exception = assertThrows(ValidationException.class, () -> {
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

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ticketService.validateTicket(ticket.getUuid(), 2L);
        });
        assertEquals(exception.getMessage(), "no authority userId");
        System.out.println("exception = " + exception.getMessage());
    }

    @Test
    @DisplayName("내부 API - 결제 취소 시 티켓 상태 변경 ( Success Case )")
    void cancelPayment_Success() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        when(ticketRepository.findByUuid(ticket.getUuid())).thenReturn(Optional.of(ticket));

        ValidationResponse validationResponse = ticketService.cancelPayment(ticket.getUuid());
        assertTrue(validationResponse.success());
        assertEquals(validationResponse.message(), "success");
    }

    @Test
    @DisplayName("내부 API - 결제 취소 시 티켓 상태 변경 ( Failure Case )")
    void cancelPayment_Failure() {
        Ticket ticket = new Ticket(1L, UUID.randomUUID(), "A1", 10000.0);
        when(ticketRepository.findByUuid(ticket.getUuid())).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ticketService.cancelPayment(ticket.getUuid());
        });

        assertEquals("not found ticket", exception.getMessage());
    }


}
