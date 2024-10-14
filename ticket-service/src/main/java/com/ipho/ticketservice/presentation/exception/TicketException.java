package com.ipho.ticketservice.presentation.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TicketException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String message;

    public TicketException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
