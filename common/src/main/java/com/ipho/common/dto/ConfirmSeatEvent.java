package com.ipho.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmSeatEvent {

    private UUID eventId;
    private String seatNumber;
    private Double price;
}
