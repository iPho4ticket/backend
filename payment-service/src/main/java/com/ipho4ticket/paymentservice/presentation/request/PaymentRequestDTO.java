package com.ipho4ticket.paymentservice.presentation.request;

import com.ipho4ticket.paymentservice.domain.model.PaymentMethod;
import java.util.UUID;

public record PaymentRequestDTO(
    UUID ticketId,
    PaymentMethod paymentMethod,
    Long amount
) {

}


