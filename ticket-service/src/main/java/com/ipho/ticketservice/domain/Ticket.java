package com.ipho.ticketservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String seatNumber;
    private Double price;

    private TicketStatus status;
    private Timestamp reservationTime;
    private Timestamp expirationTime;


    public Ticket(Long userId, UUID eventId, String seatNumber, Double price) {
        this.userId = userId;
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = TicketStatus.PENDING;
        this.reservationTime = Timestamp.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        // expirationTime 은 어떻게 설정할지?
    }

    public void cancel() {
        this.status = TicketStatus.CANCELED;
    }
}
