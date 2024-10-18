package com.ipho4ticket.eventservice.application.service.exception;

public class SearchNotExistsException extends RuntimeException {
    public SearchNotExistsException(String message) {
        super(message);
    }
}
