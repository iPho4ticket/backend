package com.ipho.common.exception.payment;

public class AccessDeniedPaymentException extends RuntimeException {
    public AccessDeniedPaymentException(String message) {
        super(message);
    }
}
