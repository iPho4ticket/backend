package com.ipho.ticketservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    }

    public void cancel() {
        this.status = TicketStatus.CANCELED;
    }
}
