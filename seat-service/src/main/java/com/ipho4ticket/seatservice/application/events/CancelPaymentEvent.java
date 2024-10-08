package com.ipho4ticket.seatservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPaymentEvent {
    // 변경 필요
    private String ticketId;
    private String seatId;
}
