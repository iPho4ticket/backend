package com.ipho4ticket.paymentservice.domain.service;

import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProcessor {
    ReadyResponse payReady(String itemName, BigDecimal totalAmount, UUID ticket_id, UUID payment_id);
    ApproveResponse payApprove(String tid, String pgToken);
}

