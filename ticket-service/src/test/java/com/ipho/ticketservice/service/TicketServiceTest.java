package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.dto.TicketInfoDto;
import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketMakingEvent;
import com.ipho.ticketservice.application.event.dto.TicketTopic;
import com.ipho.ticketservice.application.event.service.EventProducer;
import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.domain.model.Ticket;
import com.ipho.ticketservice.domain.model.TicketStatus;
import com.ipho.ticketservice.domain.repository.TicketRepository;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import com.ipho.ticketservice.infrastructure.messaging.DynamicKafkaListener;
import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.presentation.response.TicketResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class TicketServiceTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DynamicKafkaListener dynamicKafkaListener;


    @Test
    @DisplayName("티켓 예매 + Reservation 이벤트 발행")
    void reservationTicket() throws Exception {

        TicketRequestDto requestDto = new TicketRequestDto(1L, UUID.randomUUID(), "A1", 10000.0);
        Ticket ticket = new Ticket(requestDto.userId(), requestDto.eventId(), requestDto.seatNumber(), requestDto.price());
        ticketRepository.save(ticket);

        dynamicKafkaListener.startListener(TicketTopic.SEAT_BOOKING.getTopic(), requestDto.eventId());
        eventProducer.publishSeatBookingEvent(new SeatBookingEvent(ticket.getUuid(), ticket.getUserId(), ticket.getEventId(), ticket.getSeatNumber(), ticket.getPrice()));

        // seat-service 에서 event 처리 후, ticket-making-{eventId} 로 보냈다고 가정 ( test 환경에서는 직접 받아 값 처리, 메시지는 성공적으로 구독 )
        Thread.sleep(1000);
        Object seatBookingMessage = dynamicKafkaListener.getReceivedMessage();
        System.out.println("seatBookingMessage = " + seatBookingMessage);
        assertThat(seatBookingMessage).isNotNull();
        assertThat(seatBookingMessage).isInstanceOf(SeatBookingEvent.class);

        // 실제로는 seat-service 에서 feign 요청을 통해서 동적으로 topic 구독 ( 이때, 구독할 topic 은 ticket-making-{eventId} )
        dynamicKafkaListener.startListener(TicketTopic.TICKET_MAKING.getTopic(), requestDto.eventId());
        kafkaTemplate.send(TicketTopic.TICKET_MAKING.getTopic() + "-" + requestDto.eventId(), new TicketMakingEvent(ticket.getUuid(), ticket.getSeatId(), "tmp Event Name", ticket.getSeatNumber(), BigDecimal.valueOf(ticket.getPrice())));

        String topicName = TicketTopic.TICKET_MAKING.getTopic() + "-" + requestDto.eventId();
        TicketMakingEvent eventDto = new TicketMakingEvent(ticket.getUuid(), ticket.getSeatId(), "tmp Event Name", ticket.getSeatNumber(), BigDecimal.valueOf(ticket.getPrice()));

        try {
            SendResult<String, Object> result = kafkaTemplate.send(topicName, eventDto).get();
            System.out.println("Message sent successfully to topic: " + result.getRecordMetadata().topic());
            System.out.println("result = " + result.getRecordMetadata().toString());

        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
        }

        Thread.sleep(3000);
        Object ticketMakingMessage = dynamicKafkaListener.getReceivedMessage();

        System.out.println("ticketMakingMessage = " + ticketMakingMessage);
        assertThat(ticketMakingMessage).isNotNull();
        assertThat(ticketMakingMessage).isInstanceOf(TicketMakingEvent.class);
        TicketMakingEvent event = (TicketMakingEvent) ticketMakingMessage;
        assertThat(event.getTicketId()).isEqualTo(ticket.getUuid());

        ticketRepository.flush();
        assertThat(ticketRepository.findByUuid(ticket.getUuid()).get().getStatus()).isEqualTo(TicketStatus.PENDING);
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

        // received message checking
        dynamicKafkaListener.startListener(TicketTopic.CANCEL_TICKET.getTopic(), requestDto.eventId());
        try {
            SendResult<String, Object> result = kafkaTemplate.send(TicketTopic.CANCEL_TICKET.getTopic() + "-" + requestDto.eventId(),
                    new CancelTicketEvent(ticket.getSeatId(), ticket.getEventId(), ticket.getSeatNumber(), ticket.getPrice())).get();
            System.out.println("Message sent successfully to topic: " + result.getRecordMetadata().topic());
            System.out.println("result = " + result.getRecordMetadata().toString());

        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
        }

        Thread.sleep(3000);
        Object cancelTicketMessage = dynamicKafkaListener.getReceivedMessage();

        System.out.println("cancelTicketMessage = " + cancelTicketMessage);
        assertThat(cancelTicketMessage).isNotNull();
        assertThat(cancelTicketMessage).isInstanceOf(CancelTicketEvent.class);
        CancelTicketEvent event = (CancelTicketEvent) cancelTicketMessage;
        assertThat(event.getEventId()).isEqualTo(ticket.getEventId());
        assertThat(event.getSeatId()).isEqualTo(ticket.getSeatId());
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
        ticket.addEventName("EventName");
        ticket.pending();
        ticketRepository.save(ticket);

        ValidationResponse validationResponse = ticketService.completePayment(ticket.getUuid());
        assertThat(validationResponse.success()).isTrue();
        assertThat(validationResponse.message()).isEqualTo(ticket.getEventName() + ":" + ticket.getSeatNumber());

        ticketRepository.flush();

        Ticket response = ticketRepository.findByUuid(ticket.getUuid()).get();
        System.out.println("response = " + response);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.CONFIRMED);

        System.out.println("ticket = " + response.getStatus().toString());
        System.out.println("validationResponse = " + validationResponse);
    }

}
