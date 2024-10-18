package com.ipho4ticket.seatservice.application.service.exception;

public class EventNotExistsException extends IllegalArgumentException {
    public EventNotExistsException(String message) {
        super(message);
    }
}
