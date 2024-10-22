package com.ipho.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class SeatRequestDto {
    private UUID eventId;
    private Long userId;
    private String seatNumber;
    private BigDecimal price;
}