package com.ipho4ticket.seatservice.application.service.exception;

public class SeatAlreadyExistsException extends IllegalArgumentException{
    public SeatAlreadyExistsException(String message) {
        super(message);
    }
}
