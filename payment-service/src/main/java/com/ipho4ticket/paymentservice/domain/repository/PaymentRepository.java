package com.ipho4ticket.paymentservice.domain.repository;


import com.ipho4ticket.paymentservice.domain.model.Payment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, CustomPaymentRepository {

    Page<Payment> findByUserId(Long currentUserId, Pageable pageable);
}
