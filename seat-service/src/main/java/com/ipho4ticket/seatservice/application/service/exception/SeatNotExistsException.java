package com.ipho4ticket.seatservice.application.service.exception;

public class SeatNotExistsException extends IllegalArgumentException{
    public SeatNotExistsException(String message) {
        super(message);
    }
}
