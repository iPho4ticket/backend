package com.ipho4ticket.seatservice.presentation.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatRequestDto(
        UUID eventID,

        @Pattern(regexp = "^[A-Z]$", message = "Row must be a single uppercase letter (A-Z).")
        String row,

        @Min(value = 1, message = "Column must be a positive integer.")
        int column,

        BigDecimal price){
}
