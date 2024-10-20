package com.ipho4ticket.paymentservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
public class Payment {
    // TODO: 추후 공통 모듈을 통한 감사 필드 추가

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", columnDefinition = "UUID")
    private UUID paymentId; // 결제 고유 식별자

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID (외부 테이블 user와 연관)

    @Column(name= "ticket_id", nullable = false)
    private UUID ticketId; // 티켓 ID (외부 테이블 ticket과 연관)

    @Setter
    @Column(name = "tid")
    private String tid;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(name = "date", nullable = false)
    private LocalDateTime date; // 결제 시간

    @Builder
    public Payment(Long userId, UUID ticketId, BigDecimal amount, PaymentMethod method, PaymentStatus status) {
        this.userId = userId;
        this.ticketId = ticketId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus paymentStatus) {
        this.status = paymentStatus;
    }

}
