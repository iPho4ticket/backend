package com.ipho.ticketservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    private Long userId;
    private UUID seatId;
    private UUID eventId;

    private String eventName;
    private String seatNumber;
    private Double price;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    private Timestamp reservationTime;
    private Timestamp expirationTime;

    public Ticket(Long userId, UUID eventId, String seatNumber, Double price) {
        this.userId = userId;
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = TicketStatus.OPENED;
        this.reservationTime = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        this.expirationTime = Timestamp.from(LocalDateTime.now().plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
    }

    public void addEventName(String eventName) {
        this.eventName = eventName;
    }
    public void pending() {
        this.status = TicketStatus.PENDING;
    }

    public void cancel() {
        this.status = TicketStatus.CANCELED;
    }

    public void completePayment() {
        this.status = TicketStatus.CONFIRMED;
    }
}
