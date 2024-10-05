package com.ipho4ticket.seatservice.application.dto;

import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SeatResponseDto {
    private UUID seatId;
    private String seatNumber;
    private BigDecimal price;
    private SeatStatus status;

    @Builder
    public SeatResponseDto(UUID seatId, String seatNumber, BigDecimal price, SeatStatus status) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }
}
