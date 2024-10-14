package com.ipho.ticketservice.presentation.exception;

import com.ipho.common.exception.ErrorResponse;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j(topic = "ticket-exception-handler")
public class TicketExceptionHandler {

    @ExceptionHandler(TicketException.class)
    public ErrorResponse handleIllegalArgumentException(TicketException e) {
        log.error("TicketException: {}", e.getMessage());
        return new ErrorResponse(e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ValidationResponse handleValidationException(ValidationException e) {
        log.error("ValidationException: {}", e.getMessage());
        return new ValidationResponse(false, e.getMessage());
    }

}
