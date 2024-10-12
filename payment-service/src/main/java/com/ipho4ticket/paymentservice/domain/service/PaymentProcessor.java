package com.ipho4ticket.paymentservice.domain.service;

import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProcessor {
    ReadyResponse payReady(String itemName, BigDecimal totalAmount, UUID ticket_id, UUID payment_id);
    ApproveResponse payApprove(String tid, String pgToken);

    // 결제 취소
    ApproveResponse cancelPayment(String tid, Integer cancelAmount,
        Integer cancelTaxFreeAmount, Integer cancelVatAmount);
}

