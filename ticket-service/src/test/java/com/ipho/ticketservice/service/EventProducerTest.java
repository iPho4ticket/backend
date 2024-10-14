package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.dto.TicketTopic;
import com.ipho.ticketservice.application.event.service.EventProducer;
import com.ipho.ticketservice.infrastructure.messaging.DynamicKafkaListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class EventProducerTest {

    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DynamicKafkaListener dynamicKafkaListener;

    @Test
    @DisplayName("(Ticket -> Seat): 좌석 예매 이벤트 발행")
    void publishSeatBookingEvent() throws Exception {
        // 1. 이벤트에 대한 좌석 예매 요청
        SeatBookingEvent requestEvent = new SeatBookingEvent(UUID.randomUUID(), 1L, UUID.randomUUID(), "A1", 10000.0);
        // 2. 동적으로 event 전용 topic 구독
        dynamicKafkaListener.startListener(TicketTopic.SEAT_BOOKING.getTopic(), requestEvent.getEventId());
        // 3. 좌석 예매 메시지 발행
        eventProducer.publishSeatBookingEvent(requestEvent);
        // 4. 메시지 수신
        Thread.sleep(1000);
        Object receivedMessage = dynamicKafkaListener.getReceivedMessage();
        // 5. 검증
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).isInstanceOf(SeatBookingEvent.class);
        SeatBookingEvent event = (SeatBookingEvent) receivedMessage;
        assertThat(event.getEventId()).isEqualTo(requestEvent.getEventId());
        assertThat(event.getSeatNumber()).isEqualTo("A1");
    }

    @Test
    @DisplayName("(Ticket -> Seat): 티켓 취소 이벤트 발행")
    void publishCancelTicket() throws Exception {
        // 1. 이벤트에 대한 좌석 취소 요청
        CancelTicketEvent requestEvent = new CancelTicketEvent(UUID.randomUUID(), UUID.randomUUID(), "A1", 10000.0);
        // 2. 동적으로 event 전용 topic 구독
        dynamicKafkaListener.startListener(TicketTopic.CANCEL_TICKET.getTopic(), requestEvent.getEventId());
        // 3. 좌석 취소 메시지 발행
        eventProducer.publishCancelTicket(requestEvent);
        // 4. 메시지 수신
        Thread.sleep(1000);
        Object receivedMessage = dynamicKafkaListener.getReceivedMessage();
        // 5. 메시지 검증
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).isInstanceOf(CancelTicketEvent.class);
        CancelTicketEvent event = (CancelTicketEvent) receivedMessage;
        assertThat(event.getEventId()).isEqualTo(requestEvent.getEventId());
        assertThat(event.getSeatNumber()).isEqualTo("A1");

    }


}
