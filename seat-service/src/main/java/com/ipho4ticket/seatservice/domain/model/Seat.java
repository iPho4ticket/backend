package com.ipho4ticket.seatservice.domain.model;

import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="seats")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name="seat_id")
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(unique=true, nullable=false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    public static Seat create (SeatRequestDto request) {
        return Seat.builder()
                .eventId(request.eventID())
                .seatNumber(createSeatNum(request.row(), request.column()))
                .price(request.price())
                .build();
    }

    public Seat(UUID id, UUID eventId, SeatStatus status, String seatNumber, BigDecimal price) {
        this.id = id;
        this.eventId = eventId;
        this.status= status;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    private static String createSeatNum(String row, int column) {
        return row+column;
    }

    public void updateStatus(SeatStatus seatStatus){
        this.status=seatStatus;
    }

}
