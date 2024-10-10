package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.service.EventProducer;
import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TicketServiceTest {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketService ticketService;

    @Autowired
    EventProducer eventService;

    private CountDownLatch reservationLatch;
    private CountDownLatch cancelLatch;
    private SeatBookingEvent seatBookingEvent;
    private CancelTicketEvent cancelTicketEvent;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, eventService);
        reservationLatch = new CountDownLatch(1);
        cancelLatch = new CountDownLatch(1);
    }

    @KafkaListener(topics = "seat-booking", groupId = "${spring.application.name}")
    public void booking_listen(SeatBookingEvent event) {
        System.out.println("reservation Ticket Queue start");
        System.out.println("reservationLatch.getCount() = " + reservationLatch.getCount());
        seatBookingEvent = event;
        reservationLatch.countDown();
        System.out.println("reservation Ticket Queue End");
        System.out.println("reservationLatch.getCount() = " + reservationLatch.getCount());
    }

    @KafkaListener(topics = "cancel-ticket", groupId = "${spring.application.name}")
    public void cancel_listen(CancelTicketEvent event) {
        cancelTicketEvent = event;
        cancelLatch.countDown();
    }

    @Test
    @DisplayName("티켓 예매 + Reservation 이벤트 발행")
    void reservationTicket() throws Exception {
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        TicketResponseDto responseDto = ticketService.reservationTicket(requestDto);

        // ResponseDto Check
        assertThat(responseDto.ticketStatus()).isEqualTo(TicketStatus.OPENED.toString());
        assertThat(responseDto.message()).isEqualTo("Ticket reserved successfully.");

        // Event Message Checking
        boolean messageConsumed = reservationLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        SeatBookingEvent responseEvent = seatBookingEvent;
        assertThat(responseEvent).isEqualTo(new SeatBookingEvent(responseDto.ticketId(), requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price()));

        System.out.println("requestDto = " + requestDto);
        System.out.println("responseEvent = " + responseEvent);
    }

    @Test
    @DisplayName("티켓 조회")
    void searchTicketInfo() {
        // Ticket 생성
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket ticket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        ticketRepository.save(ticket);

        // Ticket 조회
        TicketInfoDto dto = ticketService.searchTicketInfo(ticket.getUuid());

        assertThat(dto.ticketId()).isEqualTo(ticket.getUuid());
        assertThat(dto.userId()).isEqualTo(requestDto.userId());
        assertThat(dto.eventId()).isEqualTo(requestDto.eventId());
        assertThat(dto.seatNumber()).isEqualTo(requestDto.seatNumber());
        assertThat(dto.price()).isEqualTo(requestDto.price());

        System.out.println("dto = " + dto);
    }

    @Test
    @DisplayName("티켓 취소 + Cancel 이벤트 발행")
    void cancelTicket() throws Exception {
        // Ticket 생성
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket ticket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        ticketRepository.save(ticket);

        // Ticket 취소
        TicketResponseDto responseDto = ticketService.cancelTicket(ticket.getUuid());

        // Cancel Ticket Response Checking
        assertThat(responseDto.ticketId()).isEqualTo(ticket.getUuid());
        assertThat(responseDto.ticketStatus()).isEqualTo(TicketStatus.CANCELED.toString());
        assertThat(responseDto.message()).isEqualTo("Ticket canceled successfully.");

        // Event Message Checking
        boolean messageConsumed = cancelLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        CancelTicketEvent responseEvent = cancelTicketEvent;

        // 여기서 SeatId 는 Seat-Service 에서 받은 Message 로 값 세팅해야 해서 해당 부분은 Test 불가
        assertThat(responseEvent.getEventId()).isEqualTo(requestDto.eventId());
        assertThat(responseEvent.getSeatNumber()).isEqualTo(requestDto.seatNumber());
        assertThat(responseEvent.getPrice()).isEqualTo(requestDto.price());

        System.out.println("requestDto = " + requestDto);
        System.out.println("responseEvent = " + responseEvent);
    }

    @Test
    @DisplayName("내부 API - 결제 전 Ticket Validation Checking")
    void validateTicket() {
        // Ticket 생성
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket ticket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        // Ticket Validation - 조건 1. Pending
        ticket.pending();
        // Ticket Validation - 조건 2. expirationTime > CurrentTime ( 현재 임의로 reservationTime + 3일로 지정 )
        ticketRepository.save(ticket);

        ValidationResponse validationResponse = ticketService.validateTicket(ticket.getUuid(), ticket.getUserId());
        assertThat(validationResponse.success()).isTrue();
        assertThat(validationResponse.message()).isEqualTo("valid ticket");

        System.out.println("validationResponse = " + validationResponse);
    }

    @Test
    @DisplayName("내부 API - 결제 후 Ticket Status 변경 ( CONFIRMED )")
    void completePayment() {
        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket ticket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        ticket.pending();
        ticketRepository.save(ticket);

        ValidationResponse validationResponse = ticketService.completePayment(ticket.getUuid());
        assertThat(validationResponse.success()).isTrue();
        assertThat(validationResponse.message()).isEqualTo("complete payment");

        ticketRepository.flush();
        Ticket response = ticketRepository.findByUuid(ticket.getUuid()).get();

        System.out.println("ticket = " + response.getStatus().toString());
        System.out.println("validationResponse = " + validationResponse);
    }


}
