package com.ipho4ticket.seatservice.domain.model;

import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="seats")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Seat{
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name="seat_id")
    private UUID seatId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name="seat_number", nullable=false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SeatStatus status;

    @Column(name="price", precision=8, scale=2, nullable = false)
    private BigDecimal price;

    public Seat(UUID eventId,String row, int column,BigDecimal price) {
        this.eventId = eventId;
        this.seatNumber = createSeatNum(row,column);
        this.price = price;
    }

    private static String createSeatNum(String row, int column) {
        return row+column;
    }

    public void updateStatus(SeatStatus seatStatus){
        this.status=seatStatus;
    }
}
