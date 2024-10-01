package com.ipho4ticket.paymentservice.infrastructure.repository;

import com.ipho4ticket.paymentservice.domain.model.Payment;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import com.ipho4ticket.paymentservice.domain.repository.CustomPaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements CustomPaymentRepository {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Page<Payment> searchWithParams(Map<String, String> searchParams, Pageable pageable) {
        // JPQL 또는 Criteria API로 커스텀 쿼리 작성
        String queryStr = "SELECT p FROM Payment p WHERE 1=1";

        if (searchParams.containsKey("status")) {
            queryStr += " AND p.status = :status";
        }

        if (searchParams.containsKey("amountMin")) {
            queryStr += " AND p.amount >= :amountMin";
        }

        if (searchParams.containsKey("amountMax")) {
            queryStr += " AND p.amount <= :amountMax";
        }

        TypedQuery<Payment> query = entityManager.createQuery(queryStr, Payment.class);

        // 파라미터 바인딩
        if (searchParams.containsKey("status")) {
            query.setParameter("status", PaymentStatus.valueOf(searchParams.get("status")));
        }

        if (searchParams.containsKey("amountMin")) {
            query.setParameter("amountMin", new BigDecimal(searchParams.get("amountMin")));
        }

        if (searchParams.containsKey("amountMax")) {
            query.setParameter("amountMax", new BigDecimal(searchParams.get("amountMax")));
        }

        // 페이징 처리
        int totalRows = query.getResultList().size();  // 전체 데이터 수
        query.setFirstResult((int) pageable.getOffset());  // 시작 인덱스
        query.setMaxResults(pageable.getPageSize());       // 한 페이지에 표시할 데이터 수

        List<Payment> payments = query.getResultList();  // 실제 결과

        return new PageImpl<>(payments, pageable, totalRows);  // 페이징된 결과 반환
    }
}



