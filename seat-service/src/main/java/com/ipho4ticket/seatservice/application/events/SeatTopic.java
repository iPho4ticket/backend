package com.ipho4ticket.seatservice.application.events;

import lombok.Getter;

@Getter
public enum SeatTopic {
    TICKET_MAKING("ticket-making");

    private final String topic;

    SeatTopic(String topic) {
        this.topic = topic;
    }
}
