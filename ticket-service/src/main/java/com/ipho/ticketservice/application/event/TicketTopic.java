package com.ipho.ticketservice.application.event;

import lombok.Getter;

@Getter
public enum TicketTopic {

    SEAT_BOOKING("seat-booking"), CANCEL_TICKET("cancel-ticket");

    private final String topic;

    TicketTopic(String topic) {
        this.topic = topic;
    }

}
