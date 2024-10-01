package com.ipho4ticket.paymentservice.domain.repository;

import com.ipho4ticket.paymentservice.domain.model.Payment;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPaymentRepository {
    Page<Payment> searchWithParams(Map<String, String> searchParams, Pageable pageable);
}

