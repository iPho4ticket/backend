package com.ipho4ticket.paymentservice.presentation.response;

import com.ipho4ticket.paymentservice.domain.model.PaymentMethod;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentResponseDTO {

    private UUID paymentId;
    private Long userId;
    private UUID ticketId;
    private String tid;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime date;

    @Builder
    public PaymentResponseDTO(UUID paymentId, Long userId, UUID ticketId,String tid, BigDecimal amount,
        PaymentMethod method, PaymentStatus status, LocalDateTime date) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.ticketId = ticketId;
        this.tid = tid;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.date = date;
    }
}

