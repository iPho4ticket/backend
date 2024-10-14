package com.ipho.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j(topic = "global exception handler")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleGlobalException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "IllegalArgumentException");
        return ResponseEntity.status(errorResponse.getHttpStatus()).body(errorResponse);
    }

}
