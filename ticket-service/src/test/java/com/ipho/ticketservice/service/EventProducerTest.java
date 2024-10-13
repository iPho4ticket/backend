package com.ipho.ticketservice.service;

import com.ipho.ticketservice.application.event.dto.CancelTicketEvent;
import com.ipho.ticketservice.application.event.dto.SeatBookingEvent;
import com.ipho.ticketservice.application.event.service.EventProducer;
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
public class EventProducerTest {

    @Autowired
    private EventProducer producer;

    private CountDownLatch bookingLatch;
    private CountDownLatch cancelLatch;
    private SeatBookingEvent seatBookingEvent;
    private CancelTicketEvent cancelTicketEvent;

    @BeforeEach
    void setUp() {
        bookingLatch = new CountDownLatch(1);
        cancelLatch = new CountDownLatch(1);
    }


    @KafkaListener(topics = "seat-booking", groupId = "${spring.application.name}")
    public void booking_listen(SeatBookingEvent event) {
        seatBookingEvent = event;
        bookingLatch.countDown();
    }

    @KafkaListener(topics = "cancel-ticket", groupId = "${spring.application.name}")
    public void cancel_listen(CancelTicketEvent event) {
        cancelTicketEvent = event;
        cancelLatch.countDown();
    }

    @Test
    @DisplayName("(Ticket -> Seat): 좌석 예매 이벤트 발행")
    void publishSeatBookingEvent() throws Exception {
        SeatBookingEvent requestEvent = new SeatBookingEvent(UUID.randomUUID(), 1L, UUID.randomUUID(), "A1", 10000.0);
        producer.publishSeatBookingEvent(requestEvent.getEventId(), requestEvent);

        boolean messageConsumed = bookingLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        SeatBookingEvent responseEvent = seatBookingEvent;
        assertThat(responseEvent).isEqualTo(requestEvent);


        System.out.println("requestEvent = " + requestEvent);
        System.out.println("responseEvent = " + responseEvent);
    }

    @Test
    @DisplayName("(Ticket -> Seat): 티켓 취소 이벤트 발행")
    void publishCancelTicket() throws Exception {
        CancelTicketEvent requestEvent = new CancelTicketEvent(UUID.randomUUID(), UUID.randomUUID(), "A1", 10000.0);
        producer.publishCancelTicket(requestEvent.getEventId(), requestEvent);

        boolean messageConsumed = cancelLatch.await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);
        CancelTicketEvent responseEvent = cancelTicketEvent;
        assertThat(responseEvent).isEqualTo(requestEvent);

        System.out.println("requestEvent = " + requestEvent);
        System.out.println("responseEvent = " + responseEvent);
    }


}
