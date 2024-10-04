package com.ipho4ticket.seatservice.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SeatResponseDto implements Serializable {
    private UUID seatId;
    private String seatNumber;
    private BigDecimal price;
    private SeatStatus status;

    @Builder
    @JsonCreator // Jackson이 사용할 수 있도록 지정
    public SeatResponseDto(
            @JsonProperty("seatId") UUID seatId,
            @JsonProperty("seatNumber") String seatNumber,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("status") SeatStatus status) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }
}
