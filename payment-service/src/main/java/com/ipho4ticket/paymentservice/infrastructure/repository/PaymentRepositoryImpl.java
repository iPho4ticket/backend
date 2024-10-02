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
        // 페이징 처리를 위해 COUNT 쿼리
        String countQueryStr = "SELECT COUNT(p) FROM Payment p WHERE 1=1";
        String queryStr = "SELECT p FROM Payment p WHERE 1=1";

        // 조건 추가
        if (searchParams.containsKey("status")) {
            countQueryStr += " AND p.status = :status";
            queryStr += " AND p.status = :status";
        }

        if (searchParams.containsKey("amountMin")) {
            countQueryStr += " AND p.amount >= :amountMin";
            queryStr += " AND p.amount >= :amountMin";
        }

        if (searchParams.containsKey("amountMax")) {
            countQueryStr += " AND p.amount <= :amountMax";
            queryStr += " AND p.amount <= :amountMax";
        }

        // Count 쿼리 실행
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);

        // 파라미터 바인딩
        if (searchParams.containsKey("status")) {
            PaymentStatus status = PaymentStatus.valueOf(searchParams.get("status"));
            countQuery.setParameter("status", status);
        }

        if (searchParams.containsKey("amountMin")) {
            BigDecimal amountMin = new BigDecimal(searchParams.get("amountMin"));
            countQuery.setParameter("amountMin", amountMin);
        }

        if (searchParams.containsKey("amountMax")) {
            BigDecimal amountMax = new BigDecimal(searchParams.get("amountMax"));
            countQuery.setParameter("amountMax", amountMax);
        }

        // 전체 데이터 수
        long totalRows = countQuery.getSingleResult();

        // 실제 데이터 쿼리 실행
        TypedQuery<Payment> query = entityManager.createQuery(queryStr, Payment.class);

        // 파라미터 바인딩 (다시 설정)
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
        query.setFirstResult((int) pageable.getOffset());  // 시작 인덱스
        query.setMaxResults(pageable.getPageSize());       // 한 페이지에 표시할 데이터 수

        List<Payment> payments = query.getResultList();  // 실제 결과

        return new PageImpl<>(payments, pageable, totalRows);  // 페이징된 결과 반환
    }
}




