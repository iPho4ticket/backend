package com.ipho4ticket.clienteventfeign.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDto(
        UUID eventId,
        String title,
        String description,
        LocalDate date,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
