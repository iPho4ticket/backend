package com.ipho.ticketservice.application.event.dto;

import lombok.Getter;

@Getter
public enum TicketTopic {

    SEAT_BOOKING("seat-booking"), CANCEL_TICKET("cancel-ticket"), TICKET_MAKING("ticket-making");

    private final String topic;

    TicketTopic(String topic) {
        this.topic = topic;
    }

}
