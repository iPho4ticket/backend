package com.ipho4ticket.eventservice.presentation.request;

import com.ipho4ticket.eventservice.domain.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventRequestDto(
    String title,
    String description,
    LocalDate date,
    LocalDateTime startTime,
    LocalDateTime endTime){
}