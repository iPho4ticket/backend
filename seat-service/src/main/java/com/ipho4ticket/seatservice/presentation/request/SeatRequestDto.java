package com.ipho4ticket.seatservice.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatRequestDto(
        @NotNull(message = "필수 입력정보입니다.")
        UUID eventId,

        @NotNull(message = "필수 입력정보입니다.")
        @Pattern(regexp = "^[A-Z]$", message = "Row must be a single uppercase letter (A-Z).")
        String row,

        @NotNull(message = "필수 입력정보입니다.")
        @Min(value = 1, message = "Column must be a positive integer.")
        int column,

        @NotNull(message = "필수 입력정보입니다.")
        BigDecimal price){
}
