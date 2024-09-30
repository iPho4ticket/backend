package com.ipho.ticketservice;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.application.dto.TicketRequestDto;
import com.ipho.ticketservice.application.dto.TicketResponseDto;
import com.ipho.ticketservice.application.event.CancelTicketEvent;
import com.ipho.ticketservice.application.event.SeatBookingEvent;
import com.ipho.ticketservice.application.service.EventService;
import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.domain.Ticket;
import com.ipho.ticketservice.domain.TicketRepository;
import com.ipho.ticketservice.domain.TicketStatus;
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
public class TicketServiceTest {

    @Autowired
    private TicketService ticketService;
    private TicketRepository ticketRepository;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventService = mock(EventService.class);
        ticketService = new TicketService(ticketRepository, eventService);
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
        verify(eventService, times(1)).publishSeatBookingEvent(any(SeatBookingEvent.class));
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

        verify(eventService, times(1)).publishCancelTicket(any(CancelTicketEvent.class));
    }

    
    
    
    
}
